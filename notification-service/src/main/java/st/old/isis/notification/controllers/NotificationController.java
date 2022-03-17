package st.old.isis.notification.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.notification.services.NotificationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

  private final NotificationService service;

  @PostMapping
  public void create(@RequestBody @NonNull final List<NotificationRequestDto> requests) {
    service.sendNotification(requests);
  }
}
