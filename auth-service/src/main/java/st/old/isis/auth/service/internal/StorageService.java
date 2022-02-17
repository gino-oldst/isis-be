package st.old.isis.auth.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.auth.builder.StorageUrl;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.exception.IsisException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  private final StorageUrl storageUrl;
  private final InternalRestService restService;

  public AppUserDto getByUsername(@NonNull final String username) throws IsisException {

    final var url = storageUrl.auth().getAppUserInfoAuthenticationUrl(username);

    return restService.get(url, AppUserDto.class);
  }
}
