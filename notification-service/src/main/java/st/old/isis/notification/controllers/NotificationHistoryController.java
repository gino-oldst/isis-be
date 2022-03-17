package st.old.isis.notification.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.notification.NotificationHistoryDto;
import st.old.isis.exception.IsisException;
import st.old.isis.notification.services.NotificationHistoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification-history")
public class NotificationHistoryController {

  private final NotificationHistoryService service;

  @GetMapping("/list/{id}")
  public ListDto<NotificationHistoryDto> searchHistory(
      @PathVariable @NonNull final String recipient, Pageable pageable) throws IsisException {
    return service.search(recipient, pageable);
  }
}
