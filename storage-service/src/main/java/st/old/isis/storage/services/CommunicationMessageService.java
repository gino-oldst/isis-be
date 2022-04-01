package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationMessageService {

  private final CommunicationMessageRepository repository;

  private final CommunicationRecipientService communicationRecipientService;

  public List<CommunicationMessageDto> batchSave(
      @NonNull final List<CommunicationMessageDto> request) {

    final var messages = request.stream().map(this::mapToEntity).collect(Collectors.toList());

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
  public ListDto<CommunicationMessageDto> search(
      @NonNull @Audited final CommunicationMessageDto filter) {

    final var spec = new CommunicationMessageSpecification(filter);

    final var rawResults =
        repository.findAll(spec).stream().map(this::mapToResource).collect(Collectors.toList());

    var filteredResults = rawResults;

    if (!ListUtils.isNullOrEmpty(filter.getRecipients())) {

      filteredResults =
          rawResults.stream()
              .filter(
                  message -> {
                    final var recpfilter =
                        CommunicationRecipientDto.builder()
                            .chainLinkId(message.getChainLinkId())
                            .build();
                    final var commRecpList = communicationRecipientService.search(recpfilter);

                    final var recpList =
                        commRecpList.getData().stream()
                            .map(CommunicationRecipientDto::getRecipient)
                            .collect(Collectors.toList());

                    return recpList.containsAll(filter.getRecipients());
                  })
              .collect(Collectors.toList());
    }

    final var size = filteredResults.size();

    return ListDto.<CommunicationMessageDto>builder()
        .data(filteredResults)
        .totalNoOfRecords(size)
        .totalPages(size)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "SEARCH_COMMUNICATION_MESSAGE")
  public ListDto<CommunicationMessageDto> search(
      @NonNull @Audited final CommunicationMessageDto filter, Pageable pageable) {

    final var spec = new CommunicationMessageSpecification(filter);

    final var searchResults = repository.findAll(spec, pageable);

    final var results =
            searchResults.stream().map(this::mapToResource).collect(Collectors.toList());

    return ListDto.<CommunicationMessageDto>builder()
            .data(results)
            .totalNoOfRecords(searchResults.getTotalElements())
            .totalPages(searchResults.getTotalPages())
            .totalCurrentElements(searchResults.getNumberOfElements())
            .build()
            .success();

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

  private CommunicationMessageDto mapToResource(@NonNull final CommunicationMessage entity) {
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
}
