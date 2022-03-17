package st.old.isis.storage.services.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.storage.builder.NotificationUrl;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationUrl notificationUrl;
  private final InternalRestService restService;
  private final ObjectUtils objectUtils;

  @Async
  public void sendNotification(@NonNull final NotificationRequestDto request) {

    try {

      final var url = notificationUrl.notification().root();

      final var entity =
          HttpEntityBuilder.builder().jsonContent().body(objectUtils.toString(request)).build();

      restService.post(url, entity, Void.class);
    } catch (Throwable e) {
      log.error("Error sending notification", e);
      e.printStackTrace();
    }
  }
}
