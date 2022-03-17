package st.old.isis.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.notification.NotificationHistoryDto;
import st.old.isis.notification.entities.NotificationHistory;
import st.old.isis.notification.repositories.NotificationHistoryRepository;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

  private final NotificationHistoryRepository repository;

  public NotificationHistoryDto create(@NonNull final NotificationHistoryDto history) {

    final var notificationHistory = mapToEntity(history);

    final var createdHistory = repository.save(notificationHistory);

    return mapToResource(createdHistory);
  }

  public ListDto<NotificationHistoryDto> search(
      @NonNull final String recipient, Pageable pageable) {

    final var results = repository.findByRecipient(recipient, pageable);

    final var data = results.get().map(this::mapToResource).collect(Collectors.toList());

    return ListDto.<NotificationHistoryDto>builder()
        .totalCurrentElements(results.getNumberOfElements())
        .totalNoOfRecords(results.getTotalElements())
        .totalPages(results.getTotalPages())
        .data(data)
        .build();
  }

  private NotificationHistory mapToEntity(@NonNull final NotificationHistoryDto dto) {
    return NotificationHistory.builder()
        .subject(dto.getSubject())
        .message(dto.getMessage())
        .recipient(dto.getRecipient())
        .dateSent(dto.getDateSent())
        .build();
  }

  private NotificationHistoryDto mapToResource(@NonNull final NotificationHistory entity) {
    return NotificationHistoryDto.builder()
        .subject(entity.getSubject())
        .message(entity.getMessage())
        .recipient(entity.getRecipient())
        .dateSent(entity.getDateSent())
        .build();
  }
}
