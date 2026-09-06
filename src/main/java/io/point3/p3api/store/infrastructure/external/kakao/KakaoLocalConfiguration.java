package io.point3.p3api.store.infrastructure.external.kakao;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoLocalConfiguration {

  @Bean
  public HttpClient kakaoLocalHttpClient() {
    return HttpClient.newBuilder().build();
  }
}
