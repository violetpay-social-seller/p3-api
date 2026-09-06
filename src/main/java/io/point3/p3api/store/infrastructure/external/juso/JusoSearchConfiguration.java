package io.point3.p3api.store.infrastructure.external.juso;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JusoSearchProperties.class)
public class JusoSearchConfiguration {

  @Bean
  public HttpClient jusoSearchHttpClient() {
    return HttpClient.newBuilder().build();
  }
}
