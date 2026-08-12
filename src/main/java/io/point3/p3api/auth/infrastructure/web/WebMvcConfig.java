package io.point3.p3api.auth.infrastructure.web;

import java.util.List;

import io.point3.p3api.common.tenant.web.CurrentStoreIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final CurrentUserArgumentResolver currentUserArgumentResolver;
  private final CurrentStoreIdArgumentResolver currentStoreIdArgumentResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserArgumentResolver);
    resolvers.add(currentStoreIdArgumentResolver);
  }
}
