package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AppUserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AppUserService service;

  @PostMapping
  public AppUserDto create(@RequestBody final AppUserDto user) throws IsisException {
    return service.create(user);
  }

  @GetMapping("/{username}")
  public AppUserDto getByUsername(@PathVariable final String username) throws IsisException {
    return service.getAuthenticationDetails(username);
  }
}
