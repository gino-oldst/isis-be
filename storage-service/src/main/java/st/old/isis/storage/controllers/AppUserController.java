package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
