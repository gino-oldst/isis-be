package st.old.isis.notification.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import st.old.isis.utils.Base64Utils;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class FcmConfiguration {

  @Value("${isis.internal.services.notification.fcm-key}")
  private String fcmKey;

  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {
    final var googleCredentials =
        GoogleCredentials.fromStream(new Base64Utils(fcmKey).getInputStream());
    final var firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
    final var app = FirebaseApp.initializeApp(firebaseOptions, "mariana-isis");
    return FirebaseMessaging.getInstance(app);
  }
}
