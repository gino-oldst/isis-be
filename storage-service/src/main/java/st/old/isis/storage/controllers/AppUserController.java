package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AppUserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/app-user")
public class AppUserController {

  private final AppUserService service;

  @PutMapping
  public AppUserDto update(@RequestBody final AppUserDto user) throws IsisException {
    return service.update(user);
  }

  @PostMapping
  public ListDto<AppUserDto> search(@RequestBody final AppUserDto filter) {return service.search(filter);}
}
