package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.MessageActivityLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.MessageActivityLogService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/message-activity")
public class MessageActivityLogController {

  private final MessageActivityLogService service;

  @PostMapping
  public MessageActivityLogDto create(@RequestBody final MessageActivityLogDto log)
      throws IsisException {
    return service.create(log);
  }

  @PutMapping
  public MessageActivityLogDto update(@RequestBody final MessageActivityLogDto log)
      throws IsisException {
    return service.update(log);
  }

  @GetMapping("/{id}")
  public MessageActivityLogDto getById(@PathVariable final String id) throws IsisException {
    return service.getById(id);
  }

  @PostMapping("/search")
  public ListDto<MessageActivityLogDto> search(@RequestBody final MessageActivityLogDto filter) {
    return service.search(filter);
  }

  @PostMapping("/search/paged")
  public ListDto<MessageActivityLogDto> pagedSearch(
      @RequestBody final MessageActivityLogDto filter, Pageable pageable) {
    return service.search(filter, pageable);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final String id) throws IsisException {
    service.delete(id);
  }
}
