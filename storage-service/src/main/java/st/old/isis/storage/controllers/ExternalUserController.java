package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.dto.storage.TempDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.ExternalUserService;
import st.old.isis.utils.ObjectUtils;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/external")
public class ExternalUserController {

  private final ExternalUserService service;
  private final ObjectUtils objectUtils;

  @PostMapping("/batch")
  public ListDto<ExternalUserDto> batchedSave(@RequestBody List<ExternalUserDto> users) {
    try {
      return service.batchedSave(users);
    } catch (ObjectOptimisticLockingFailureException | StaleStateException ex) {
      log.error("Persistence error: {} - {}", users.size(), ex.getMessage());
    }
    return ListDto.emptyList();
  }

  public ExternalUserDto create(@RequestBody final ExternalUserDto externalUserDto) throws IsisException {
    return service.create(externalUserDto);
  }

  @PostMapping("/merge")
  public void merge(@RequestBody TempDto dto) {
    service.finalizeBatch(dto.getFileSource());
  }

  @PostMapping("/search")
  public ListDto<ExternalUserDto> search(
      @RequestBody final ExternalUserDto filter, Pageable pageable) {
    return service.search(filter, pageable.getSort());
  }

  @PostMapping("/search/paged")
  public ListDto<ExternalUserDto> pagedSearch(
      @RequestBody final ExternalUserDto filter, Pageable pageable) {
    return service.search(filter, pageable);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final String id) throws IsisException {
    service.delete(id);
  }
}
