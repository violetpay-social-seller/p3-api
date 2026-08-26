package io.point3.p3api.local;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
@Profile("local-scenario")
public class LocalScenarioJwtDecoderConfig {

  private static final String ISSUER = "http://localhost/local-scenario";

  @Bean
  public JwtDecoder localScenarioJwtDecoder(
      @Value("${p3.local-scenario.seller-token}") String sellerToken,
      @Value("${p3.local-scenario.seller.cognito-sub}") String sellerCognitoSub,
      @Value("${p3.local-scenario.seller.email}") String sellerEmail,
      @Value("${p3.local-scenario.seller.name}") String sellerName,
      @Value("${p3.local-scenario.buyer-token}") String buyerToken,
      @Value("${p3.local-scenario.buyer.cognito-sub}") String buyerCognitoSub,
      @Value("${p3.local-scenario.buyer.email}") String buyerEmail,
      @Value("${p3.local-scenario.buyer.name}") String buyerName,
      @Value("${p3.local-scenario.operator-token}") String operatorToken,
      @Value("${p3.local-scenario.operator.cognito-sub}") String operatorCognitoSub,
      @Value("${p3.local-scenario.operator.email}") String operatorEmail,
      @Value("${p3.local-scenario.operator.name}") String operatorName) {
    Map<String, LocalScenarioUser> usersByToken = Map.of(
        sellerToken, new LocalScenarioUser(sellerCognitoSub, sellerEmail, sellerName),
        buyerToken, new LocalScenarioUser(buyerCognitoSub, buyerEmail, buyerName),
        operatorToken, new LocalScenarioUser(operatorCognitoSub, operatorEmail, operatorName));

    return token -> {
      LocalScenarioUser user = usersByToken.get(token);
      if (user == null) {
        throw new BadJwtException("Unknown local scenario token");
      }

      Instant now = Instant.now();
      return Jwt.withTokenValue(token)
          .header("alg", "none")
          .issuer(ISSUER)
          .subject(user.cognitoSub())
          .claim("email", user.email())
          .claim("name", user.name())
          .issuedAt(now)
          .expiresAt(now.plus(Duration.ofHours(12)))
          .build();
    };
  }

  private record LocalScenarioUser(String cognitoSub, String email, String name) {}
}
