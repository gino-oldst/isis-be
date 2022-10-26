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
import st.old.isis.dto.storage.PatternBypassDto;
import st.old.isis.dto.storage.RecentMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.entities.PatternBypass;
import st.old.isis.storage.entities.temp.TempCommunicationMessage;
import st.old.isis.storage.processors.MessageSearchProcessor;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.repositories.StreamedCommunicationMessageRepository;
import st.old.isis.storage.repositories.temp.TempCommunicationMessageRepository;
import st.old.isis.storage.specifications.CommunicationMessageSpecification;
import st.old.isis.storage.specifications.RecentMessageSpecification;
import st.old.isis.utils.ListUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationMessageService {

  private final CommunicationMessageRepository repository;
  private final StreamedCommunicationMessageRepository streamedRepository;
  private final TempCommunicationMessageRepository tempRepository;
  private final CommunicationRecipientService communicationRecipientService;
  private final MessageSearchProcessor messageSearchProcessor;

  private final PatternBypassService patternBypassService;

  @Audited(event = "BATCH_MESSAGE_SAVE")
  public List<CommunicationMessageDto> batchSave(
      @NonNull final List<CommunicationMessageDto> request) {

    final List<PatternBypassDto> patternBypassList= patternBypassService.findWithFilter(PatternBypassDto.builder().build()).getData();

    final var messages =
        request.stream()
            .filter(message -> !Strings.isEmpty(message.getSender()))
            .map(message -> {

              if(message.getMessage() == null || message.getMessage().isEmpty()) return message;

              final var bypassMatch = patternBypassList.stream().filter(bypass -> message.getMessage().contains(bypass.getValue())).findFirst().orElse(null);
              if(bypassMatch != null){
                message.setBypassType(bypassMatch.getBypassType());
              }
              return message;
            })
            .map(this::mapToTempEntity)
            .collect(Collectors.toList());

    return tempRepository.saveAll(messages).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  @Audited(event = "FINALIZE_BATCH_SAVE_MESSAGES")
  public void finalizeBatch(String fileSource) {

    log.info("Start merging messages from [{}]", fileSource);

    final long start = System.currentTimeMillis();

    log.info("Messages added: {}", repository.merge(fileSource));
    log.info("Temporary messages deleted: {}", tempRepository.deleteAllByFileSource(fileSource));

    log.info("Merge messages time elapsed: {} ms", System.currentTimeMillis() - start);
  }

  @Audited(event = "DELETE_TEMP_COMMUNICATION_MESSAGE")
  public void deleteTempMessages(@Audited String fileSource) {

    log.info("Start deleting temp messages from [{}]", fileSource);

    final long start = System.currentTimeMillis();

    log.info("Temporary messages deleted: {}", tempRepository.deleteAllByFileSource(fileSource));

    log.info("Delete temp messages time elapsed: {} ms", System.currentTimeMillis() - start);
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
              .map(CommunicationMessageService::mapToResource)
              .collect(Collectors.toList());

    } else {
      filteredResults =
          rawResults.stream()
              .map(CommunicationMessageService::mapToResource)
              .collect(Collectors.toList());
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
    log.info(
        "Start Time: {} Search Filter: {}",
        LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()),
        filter);

    final var filteredResults = filterMessages(filter);

    final var endTime = System.currentTimeMillis() - start;

    log.info("Elapsed Time: {}ms Search Filter: {}", endTime, filter);
    return returnFilteredResults(filteredResults, pageable, endTime);
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
            .map(CommunicationMessageService::mapToResource)
            .collect(Collectors.toList());

    final var afterResults =
        repository
            .findAll(afterSpec, pageable)
            .get()
            .map(CommunicationMessageService::mapToResource)
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
        .bypassType(dto.getBypassType())
        .build();
  }

  private TempCommunicationMessage mapToTempEntity(@NonNull final CommunicationMessageDto dto) {
    return TempCommunicationMessage.builder()
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
        .fileSource(dto.getFileSource())
        .bypassType(dto.getBypassType())
        .build();
  }

  public static CommunicationMessageDto mapToResource(@NonNull final CommunicationMessage entity) {
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
        .bypassType(entity.getBypassType())
        .build();
  }

  public CommunicationMessageDto mapToResource(@NonNull final TempCommunicationMessage entity) {
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
        .bypassType(entity.getBypassType())
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

    final var comparator = comparator(pageable);
    final var sortedMessages = messages.stream().sorted(comparator).collect(Collectors.toList());

    final var totalSize = sortedMessages.size();
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

    final var pagedResults = new ArrayList<>(sortedMessages.subList(startingIndex, endingIndex));

    return ListDto.<CommunicationMessageDto>builder()
        .data(pagedResults)
        .totalNoOfRecords(totalSize)
        .totalCurrentElements(pagedResults.size())
        .totalPages(totalPages)
        .build();
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

  private List<CommunicationMessageDto> filterMessages(CommunicationMessageDto filter) {
    final var messageFilter =
        CommunicationMessageDto.builder()
            .id(filter.getId())
            .dateSentFrom(filter.getDateSentFrom())
            .dateSentTo(filter.getDateSentTo())
            .source(filter.getSource())
            .sources(filter.getSources())
            .sender(filter.getSender())
            .senders(filter.getSenders())
            .build();

    final var spec = new CommunicationMessageSpecification(messageFilter);

    final var keyword = filter.getMessage();
    final var recipients = filter.getRecipients();

    final List<CommunicationMessageDto> filteredResults;

    messageSearchProcessor.init(keyword, recipients);

    try (final var searchResults = streamedRepository.findAll(spec)) {
      searchResults.forEach(messageSearchProcessor::addToQueue);
    }

    filteredResults = messageSearchProcessor.getFilteredMessages();

    return filteredResults;
  }

  @Transactional(timeoutString = "${isis.search.timeout:60}", readOnly = true)
  public List<CommunicationMessageDto> downloadMessages(CommunicationMessageDto filter) {
    return filterMessages(filter);
  }
}
