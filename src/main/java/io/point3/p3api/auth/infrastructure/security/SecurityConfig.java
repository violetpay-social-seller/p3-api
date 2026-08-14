package io.point3.p3api.auth.infrastructure.security;

import io.point3.p3api.common.tenant.buyer.filter.PublicStoreContextFilter;
import io.point3.p3api.common.tenant.buyer.provider.PublicStoreProvider;
import io.point3.p3api.common.tenant.seller.filter.StoreContextFilter;
import io.point3.p3api.common.tenant.seller.provider.SellerStoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CurrentUserRender currentUserRender,
      SellerStoreProvider sellerStoreProvider,
      PublicStoreProvider publicStoreProvider)
      throws Exception {
    StoreContextFilter storeContextFilter =
        new StoreContextFilter(currentUserRender, sellerStoreProvider);
    PublicStoreContextFilter publicStoreContextFilter =
        new PublicStoreContextFilter(publicStoreProvider);

    return http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**")
            .permitAll()
            .requestMatchers("/error", "/actuator/health", "/ws")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/stores/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .addFilterAfter(storeContextFilter, BearerTokenAuthenticationFilter.class)
        .addFilterAfter(publicStoreContextFilter, BearerTokenAuthenticationFilter.class)
        .build();
  }
}
