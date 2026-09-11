package io.point3.p3api.account.infrastructure.external.kftc;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KftcProperties.class)
public class KftcConfiguration {

  @Bean
  public HttpClient kftcHttpClient() {
    return HttpClient.newBuilder().build();
  }
}
