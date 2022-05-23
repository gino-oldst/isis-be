package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.RecentMessageDto;
import st.old.isis.dto.storage.TempDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.processors.MessageSearchProcessor;
import st.old.isis.storage.services.CommunicationMessageService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class CommunicationMessageController {

  private final CommunicationMessageService service;
  private final MessageSearchProcessor messageSearchProcessor;

  @PostMapping("/batch")
  public List<CommunicationMessageDto> batch(
      @RequestBody final List<CommunicationMessageDto> messages) {
    return service.batchSave(messages);
  }

  @PostMapping("/merge")
  public void merge(@RequestBody TempDto dto) {
    service.finalizeBatch(dto.getFileSource());
  }

  @GetMapping("/{id}")
  public CommunicationMessageDto getById(@PathVariable final String id) throws IsisException {
    return service.getById(id);
  }

  @PostMapping("/search")
  public ListDto<CommunicationMessageDto> search(
      @RequestBody final CommunicationMessageDto filter, Pageable pageable) throws IsisException {
    return service.searchList(filter, pageable);
  }

  @PostMapping("/search/paged")
  public ListDto<CommunicationMessageDto> pagedSearch(
      @RequestBody final CommunicationMessageDto filter, Pageable pageable) throws IsisException {
    try {
      return service.search(filter, pageable);
    } catch (TransactionTimedOutException e) {
      throw IsisException.timeout(
          "Search reached timeout. Modify filters to generate smaller result",
          "RequestTimeoutException");
    } finally {
      messageSearchProcessor.destroy();
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final String id) throws IsisException {
    service.delete(id);
  }

  @PostMapping("/search/recent")
  public RecentMessageDto beforeAfter(
      @RequestBody @NonNull final CommunicationMessageDto filter, Pageable pageable)
      throws IsisException {
    return service.getSurroundingMessages(filter, pageable);
  }
}
