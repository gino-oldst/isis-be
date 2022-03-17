package st.old.isis.notification.services;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.exception.IsisException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

  private final FirebaseMessagingService firebaseMessagingService;

  public void sendPushToDevice(@NonNull final MessageDto message, @NonNull final String recipient)
      throws IsisException {
    try {
      firebaseMessagingService.pushToDevice(message, recipient);
    } catch (FirebaseMessagingException e) {
      throw IsisException.exception(e.getMessage(), e);
    }
  }

  public void sendPushToTopic(@NonNull final MessageDto message, @NonNull final String recipient)
      throws IsisException {
    try {
      firebaseMessagingService.pushToTopic(message, recipient);
    } catch (FirebaseMessagingException e) {
      throw IsisException.exception(e.getMessage(), e);
    }
  }
}
