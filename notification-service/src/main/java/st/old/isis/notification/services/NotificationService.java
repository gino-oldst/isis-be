package st.old.isis.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.*;
import st.old.isis.enums.NotificationType;
import st.old.isis.exception.IsisException;
import st.old.isis.utils.ListUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final MessageTemplateService messageTemplateService;
  private final PushService pushService;
  private final NotificationHistoryService notificationHistoryService;

  public void sendNotification(@NonNull final List<NotificationRequestDto> requests) {

    requests.forEach(
        req -> {
          try {

            if (ListUtils.isNullOrEmpty(req.getRecipients())) {
              throw IsisException.badRequest("Recipients field is empty","Invalid Request");
            }

            final var isTemplate =
                    !(req.getTemplateName() == null || req.getTemplateName().isEmpty());

            if (isTemplate) {
              sendTemplatedMessage(req);
            }

            final var isCustom = req.getCustomMessage() != null;

            if (isCustom) {
              sendCustomMessage(req);
            }

          } catch (IsisException e) {
            log.error("Error sending notification.", e);
          }
        });
  }

  private MessageTemplateDto getTemplate(@NonNull final String template) throws IsisException {
    return messageTemplateService.getByName(template);
  }

  private void sendTemplatedMessage(@NonNull final NotificationRequestDto request)
      throws IsisException {

    final var template = getTemplate(request.getTemplateName());

    for (final var recipient : request.getRecipients()) {

      final String content =
          (NotificationType.EMAIL == recipient.getType())
              ? template.getEmailTemplate()
              : template.getTextTemplate();

      final var message = MessageDto.builder().subject(template.getSubject()).body(content).build();

      setRecipient(recipient, message);
    }
  }

  private void sendCustomMessage(@NonNull final NotificationRequestDto request)
      throws IsisException {

    for (final var recipient : request.getRecipients()) {
      setRecipient(recipient, request.getCustomMessage());
    }
  }

  private void setRecipient(
      @NonNull final NotificationRecipientDto recipient, @NonNull final MessageDto message)
      throws IsisException {
    final var destination = recipient.getDestination();
    final var hasDestination = destination != null && !destination.isEmpty();

    if (hasDestination) {
      sendMessage(recipient.getType(), message, destination);
    }

    final var recipientId = recipient.getRecipientId();
    final var isUser = recipientId != null && !recipientId.isEmpty();

    if (isUser) {
      // todo: get destination from user data
      //        sendMessage(recipient.getType(), request.getCustomMessage(), destination);
    }
  }

  private void sendMessage(
      @NonNull final NotificationType type,
      @NonNull final MessageDto message,
      @NonNull final String recipient)
      throws IsisException {

    if (NotificationType.PUSH == type) {
      pushService.sendPushToDevice(message, recipient);
    }

    if (NotificationType.EMAIL == type) {
      // send email
    }

    final var history =
        NotificationHistoryDto.builder()
            .subject(message.getSubject())
            .message(message.getBody())
            .recipient(recipient)
            .dateSent(LocalDateTime.now())
            .build();

    notificationHistoryService.create(history);
  }
}
