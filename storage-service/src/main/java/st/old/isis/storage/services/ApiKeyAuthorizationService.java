package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.storage.utils.RestUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyAuthorizationService {

  private final RestUtils restUtils;

  public boolean validateApiSecretKey(String apiKey, String secretKey) {

    log.debug("Validating API Key and Secret Key");

    if (restUtils.getApiKey().equals(apiKey) && restUtils.getApiSecret().equals(secretKey)) {
      log.debug("Api Key And Secret Key Valid");
      return true;
    } else {
      log.info("Api Key And Secret Key Invalid");
      return false;
    }
  }
}
