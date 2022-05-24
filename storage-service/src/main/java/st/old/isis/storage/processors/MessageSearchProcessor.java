package st.old.isis.storage.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.storage.configurations.DatabaseConfiguration;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.CommunicationRecipientService;
import st.old.isis.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSearchProcessor {

  private final ThreadLocal<ConcurrentLinkedQueue<CommunicationMessage>> threadQueue =
      new ThreadLocal<>();
  private final CommunicationRecipientService communicationRecipientService;
  private final ThreadLocal<String> keyword = new ThreadLocal<>();
  private final ThreadLocal<List<String>> recipients = new ThreadLocal<>();
  private final ThreadLocal<List<CommunicationMessageDto>> filteredMessages = new ThreadLocal<>();
  private final DatabaseConfiguration dbConfiguration;

  public ConcurrentLinkedQueue<CommunicationMessage> getThreadQueue() {

    if (threadQueue.get() == null) {
      threadQueue.set(new ConcurrentLinkedQueue<>());
    }

    return threadQueue.get();
  }

  public void init(final String keyword, final List<String> recipients) {
    this.keyword.set(keyword);
    this.recipients.set(recipients);
    this.filteredMessages.set(new ArrayList<>());
  }

  public void destroy() {
    this.keyword.set("");
    this.recipients.set(new ArrayList<>());
    this.filteredMessages.set(new ArrayList<>());
  }

  public List<CommunicationMessageDto> getFilteredMessages() {

    final var queue = getThreadQueue();

    if (queue.size() > 0) {
      processSearch(queue.size());
    }

    return filteredMessages.get();
  }

  public void addToQueue(@NonNull final CommunicationMessage message) {

    final var queue = getThreadQueue();

    queue.offer(message);

    final var batchSize = dbConfiguration.getBatchSize();

    if (getThreadQueue().size() >= batchSize) {
      final var cycle = queue.size() / batchSize;

      for (int x = 0; x < cycle; x++) {
        processSearch(batchSize);
      }
    }
  }

  public void processSearch(final int size) {
    final List<CommunicationMessage> batch = new ArrayList<>();
    final var queue = getThreadQueue();

    for (int x = 0; x < size; x++) {
      Optional.ofNullable(queue.poll()).ifPresent(batch::add);
    }

    final var filteredMessages = filter(batch, this.keyword.get(), this.recipients.get());

    this.filteredMessages.get().addAll(filteredMessages);
  }

  private List<CommunicationMessageDto> filter(
      List<CommunicationMessage> searchResults, String keyword, List<String> recipients) {

    final var chains =
        searchResults.stream()
            .map(CommunicationMessage::getChainLinkId)
            .distinct()
            .collect(Collectors.toList());

    if (!ListUtils.isNullOrEmpty(recipients)) {

      final var recipientFilter =
          CommunicationRecipientDto.builder().chains(chains).recipients(recipients).build();

      final var rec = communicationRecipientService.search(recipientFilter).getData();

      final var recipientMap =
          rec.stream()
              .collect(
                  Collectors.toMap(
                      CommunicationRecipientDto::getChainLinkId,
                      (recipient) -> new ArrayList<>(List.of(recipient.getRecipient())),
                      (old, latest) -> {
                        old.addAll(latest);
                        return old;
                      }));

      return searchResults.stream()
          .filter(message -> recipientMap.containsKey(message.getChainLinkId()))
          .filter(message -> messageContainsKeyword(message.getMessage(), keyword))
          .map(CommunicationMessageService::mapToResource)
          .peek(
              message ->
                  message.setRecipients(
                      recipientMap.getOrDefault(message.getChainLinkId(), new ArrayList<>())))
          .collect(Collectors.toList());
    }

    return searchResults.stream()
        .filter(message -> messageContainsKeyword(message.getMessage(), keyword))
        .map(CommunicationMessageService::mapToResource)
        .collect(Collectors.toList());
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
}
