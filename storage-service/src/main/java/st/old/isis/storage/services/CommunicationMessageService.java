package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import st.old.isis.annotations.Audited;
import st.old.isis.context.AuditContext;
import st.old.isis.dto.audit.AuditEvent;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.RecentMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.specifications.CommunicationMessageSpecification;
import st.old.isis.storage.specifications.RecentMessageSpecification;
import st.old.isis.utils.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationMessageService {

  private final CommunicationMessageRepository repository;

  private final CommunicationRecipientService communicationRecipientService;

  @Audited(event = "BATCH_MESSAGE_SAVE")
  public List<CommunicationMessageDto> batchSave(
      @NonNull final List<CommunicationMessageDto> request) {

    final var messages =
        request.stream()
            .filter(message -> !Strings.isEmpty(message.getSender()))
            .map(this::mapToEntity)
            .collect(Collectors.toList());

    return repository.saveAll(messages).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  @Audited(event = "CREATE_COMMUNICATION_MESSAGE")
  public CommunicationMessageDto create(@NonNull final CommunicationMessageDto dto) {

    final var entity = mapToEntity(dto);

    final var createdEntity = repository.save(entity);

    return mapToResource(createdEntity);
  }

  @Audited(event = "UPDATE_COMMUNICATION_MESSAGE")
  public CommunicationMessageDto update(@NonNull final CommunicationMessageDto dto)
      throws IsisException {

    final var oldEntity = findById(dto.getId());

    final var newEntity = mapToEntity(dto);

    final var updatedEntity = repository.save(newEntity);

    return mapToResource(updatedEntity);
  }

  @Audited(event = "GET_COMMUNICATION_MESSAGE_BY_ID")
  public CommunicationMessageDto getById(@NonNull @Audited final String id) throws IsisException {

    final var entity = findById(id);

    return mapToResource(entity);
  }

  @Audited(event = "DELETE_COMMUNICATION_MESSAGE")
  public void delete(@NonNull @Audited final String id) throws IsisException {

    final var entity = findById(id);

    repository.delete(entity);
  }

  @Audited(event = "SEARCH_COMMUNICATION_MESSAGE")
  public ListDto<CommunicationMessageDto> searchList(
      @NonNull @Audited final CommunicationMessageDto filter, Pageable pageable)
      throws IsisException {

    final var spec = new CommunicationMessageSpecification(filter);

    final var rawResults = repository.findAll(spec);

    final List<CommunicationMessageDto> filteredResults;

    if (!ListUtils.isNullOrEmpty(filter.getRecipients())) {

      final var chains =
          rawResults.stream()
              .map(CommunicationMessage::getChainLinkId)
              .collect(Collectors.toList());

      if (chains.size() > 2000) {
        throw IsisException.badRequest(
            "Search parameter exceeded. Please modify filter", "SearchFilterException");
      }

      final var recFilter = CommunicationRecipientDto.builder().chains(chains).build();

      final var recipientMap =
          communicationRecipientService.search(recFilter).getData().stream()
              .collect(
                  Collectors.toMap(
                      CommunicationRecipientDto::getChainLinkId,
                      (recipient) -> new ArrayList<>(List.of(recipient.getRecipient())),
                      (old, latest) -> {
                        old.addAll(latest);
                        return old;
                      }));

      filteredResults =
          rawResults.stream()
              .filter(message -> recipientMap.containsKey(message.getChainLinkId()))
              .map(this::mapToResource)
              .collect(Collectors.toList());

    } else {
      filteredResults = rawResults.stream().map(this::mapToResource).collect(Collectors.toList());
    }

    final var size = filteredResults.size();

    final var comparator = comparator(pageable);

    final var sortedList = filteredResults.stream().sorted(comparator).collect(Collectors.toList());

    return ListDto.<CommunicationMessageDto>builder()
        .data(sortedList)
        .totalNoOfRecords(size)
        .totalPages(sortedList.isEmpty() ? 0 : 1)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "SEARCH_COMMUNICATION_MESSAGE")
  @Transactional(timeoutString = "${isis.search.timeout:60}", readOnly = true)
  public ListDto<CommunicationMessageDto> search(
      @NonNull @Audited final CommunicationMessageDto filter, Pageable pageable)
      throws IsisException {

    final long start = System.currentTimeMillis();

    final Map<String, List<String>> recipientMap;

    final var recipients = filter.getRecipients();

    final var messageFilter =
        CommunicationMessageDto.builder()
            .dateSentFrom(filter.getDateSentFrom())
            .dateSentTo(filter.getDateSentTo())
            .source(filter.getSource())
            .sources(filter.getSources())
            .sender(filter.getSender())
            .senders(filter.getSenders())
            .build();

    final var spec = new CommunicationMessageSpecification(messageFilter);

    final var searchResults = repository.findAll(spec);

    final var keyword = filter.getMessage();

    final var chains =
        searchResults.stream()
            .map(CommunicationMessage::getChainLinkId)
            .distinct()
            .collect(Collectors.toList());

    if (chains.size() > 2000) {
      throw IsisException.badRequest(
          "Search parameter exceeded. Please modify filter", "SearchFilterException");
    }

    if (!ListUtils.isNullOrEmpty(recipients)) {

      final var recipientFilter =
          CommunicationRecipientDto.builder()
              .chains(chains)
              .recipients(filter.getRecipients())
              .build();

      final var rec = communicationRecipientService.search(recipientFilter).getData();

      recipientMap =
          rec.stream()
              .collect(
                  Collectors.toMap(
                      CommunicationRecipientDto::getChainLinkId,
                      (recipient) -> new ArrayList<>(List.of(recipient.getRecipient())),
                      (old, latest) -> {
                        old.addAll(latest);
                        return old;
                      }));

      final var filteredResults =
          searchResults.stream()
              .filter(message -> recipientMap.containsKey(message.getChainLinkId()))
              .filter(message -> messageContainsKeyword(message.getMessage(), keyword))
              .map(this::mapToResource)
              .peek(
                  message ->
                      message.setRecipients(
                          recipientMap.getOrDefault(message.getChainLinkId(), new ArrayList<>())))
              .collect(Collectors.toList());

      return returnFilteredResults(filteredResults, pageable, System.currentTimeMillis() - start);
    }

    final var filteredResults =
        searchResults.stream()
            .filter(message -> messageContainsKeyword(message.getMessage(), keyword))
            .map(this::mapToResource)
            .collect(Collectors.toList());

    return returnFilteredResults(filteredResults, pageable, System.currentTimeMillis() - start);
  }

  private ListDto<CommunicationMessageDto> returnFilteredResults(
      List<CommunicationMessageDto> results, Pageable pageable, long executionTime) {

    AuditContext.addEvent(
        AuditEvent.builder()
            .event("SEARCH_RESULTS")
            .metadata(
                String.format(
                    "{\"executionTime\": %s \"resultSize\": %s}", executionTime, results.size()))
            .build());

    return pagedResults(results, pageable);
  }

  @Audited(event = "GET_SURROUNDING_MESSAGES")
  public RecentMessageDto getSurroundingMessages(
      @NonNull final CommunicationMessageDto filter, Pageable pageable) throws IsisException {

    final var message = findById(filter.getId());

    final var timeSent = message.getTimeSent();
    final var chainLinkId = message.getChainLinkId();

    final var beforeSpec = new RecentMessageSpecification(chainLinkId, timeSent, false);
    final var afterSpec = new RecentMessageSpecification(chainLinkId, timeSent, true);

    final var beforeResults =
        repository
            .findAll(beforeSpec, pageable)
            .get()
            .map(this::mapToResource)
            .collect(Collectors.toList());

    final var afterResults =
        repository
            .findAll(afterSpec, pageable)
            .get()
            .map(this::mapToResource)
            .collect(Collectors.toList());

    Collections.reverse(beforeResults);

    return RecentMessageDto.builder()
        .message(mapToResource(message))
        .before(beforeResults)
        .after(afterResults)
        .build();
  }

  private CommunicationMessage findById(@NonNull final String id) throws IsisException {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find a message with id: [%s]", id), "NotFoundException"));
  }

  private CommunicationMessage mapToEntity(@NonNull final CommunicationMessageDto dto) {
    return CommunicationMessage.builder()
        .id(dto.getId())
        .chainLinkId(dto.getChainLinkId())
        .message(dto.getMessage())
        .sourceMessageId(dto.getSourceMessageId())
        .timeSent(dto.getTimeSent())
        .importDate(dto.getImportDate())
        .sender(dto.getSender())
        .subject(dto.getSubject())
        .source(dto.getSource())
        .type(dto.getType())
        .metadata(dto.getMetadata())
        .build();
  }

  public CommunicationMessageDto mapToResource(@NonNull final CommunicationMessage entity) {
    return CommunicationMessageDto.builder()
        .id(entity.getId())
        .chainLinkId(entity.getChainLinkId())
        .message(entity.getMessage())
        .sourceMessageId(entity.getSourceMessageId())
        .timeSent(entity.getTimeSent())
        .importDate(entity.getImportDate())
        .sender(entity.getSender())
        .subject(entity.getSubject())
        .source(entity.getSource())
        .type(entity.getType())
        .metadata(entity.getMetadata())
        .build();
  }

  private int startingIndex(int totalSize, int pageNum, int pageSize) {

    if (pageNum == 0) {
      return 0;
    }

    var index = pageNum * pageSize;

    return (index > totalSize) ? (pageNum - 1) * pageSize : index;
  }

  private int endingIndex(int totalSize, int startingIndex, int pageSize) {

    final var index = startingIndex + pageSize;

    return Math.min(index, totalSize);
  }

  private ListDto<CommunicationMessageDto> pagedResults(
      final List<CommunicationMessageDto> messages, Pageable pageable) {

    if (messages.isEmpty()) {
      return ListDto.emptyList();
    }

    final var totalSize = messages.size();
    final var pageSize = pageable.getPageSize();
    final var pageNum = pageable.getPageNumber();

    final var startingIndex = startingIndex(totalSize, pageNum, pageSize);
    final var endingIndex = endingIndex(totalSize, startingIndex, pageSize);

    var totalPages = totalSize / pageSize + ((totalSize % pageSize > 0) ? 1 : 0);

    if (startingIndex > endingIndex) {
      return ListDto.<CommunicationMessageDto>builder()
          .totalNoOfRecords(totalSize)
          .totalCurrentElements(0)
          .totalPages(totalPages)
          .data(new ArrayList<>())
          .build()
          .success();
    }

    final var comparator = comparator(pageable);

    final var pagedResults =
        messages.subList(startingIndex, endingIndex).stream()
            .sorted(comparator)
            .collect(Collectors.toList());

    return ListDto.<CommunicationMessageDto>builder()
        .data(pagedResults)
        .totalNoOfRecords(totalSize)
        .totalCurrentElements(pagedResults.size())
        .totalPages(totalPages)
        .build();
  }

  private boolean messageContainsKeyword(String message, String keyword) {

    if (Objects.isNull(message)) {
      return false;
    }

    return Strings.isEmpty(keyword) || containsIgnoreCase(message, keyword);
  }

  public static boolean containsIgnoreCase(String source, String word) {
    final var length = word.length();
    if (length == 0) {
      return true;
    }

    final var firstLo = Character.toLowerCase(word.charAt(0));
    final var firstUp = Character.toUpperCase(word.charAt(0));

    for (int i = source.length() - length; i >= 0; i--) {

      final var ch = source.charAt(i);

      if (ch != firstLo && ch != firstUp) {
        continue;
      }

      if (source.regionMatches(true, i, word, 0, length)) {
        return true;
      }
    }

    return false;
  }

  private Comparator<CommunicationMessageDto> comparator(Pageable pageable) {
    return pageable
        .getSort()
        .get()
        .map(
            s -> {
              final var property = s.getProperty();
              final var direction = s.getDirection();

              final var com = CommunicationMessageDto.SORT_FIELDS.get(property);

              if (com == null) {
                return null;
              }

              if (direction.isDescending()) {
                return com.reversed();
              }

              return com;
            })
        .filter(Objects::nonNull)
        .reduce(Comparator::thenComparing)
        .orElse(CommunicationMessageDto.SORT_FIELDS.get("timeSent"));
  }
}
