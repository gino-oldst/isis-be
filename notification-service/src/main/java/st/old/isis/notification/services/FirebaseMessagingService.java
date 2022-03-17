package st.old.isis.notification.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.MessageDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

  private final FirebaseMessaging firebaseMessaging;

  public String pushToDevice(@NonNull final MessageDto message, @NonNull final String recipient)
      throws FirebaseMessagingException {

    final var notification =
        Notification.builder().setTitle(message.getSubject()).setBody(message.getBody()).build();

    final var pushMessage =
        Message.builder()
            .setToken(recipient)
            .setNotification(notification)
            .putAllData(message.getData())
            .build();

    return firebaseMessaging.send(pushMessage);
  }

  public String pushToTopic(@NonNull final MessageDto message, @NonNull final String recipient)
      throws FirebaseMessagingException {

    final var notification =
        Notification.builder().setTitle(message.getSubject()).setBody(message.getBody()).build();

    final var pushMessage =
        Message.builder()
            .setTopic(recipient)
            .setNotification(notification)
            .putAllData(message.getData())
            .build();

    return firebaseMessaging.send(pushMessage);
  }
}
