package io.point3.p3api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtCommandExtractorTest {

  private final JwtCommandExtractor extractor = new JwtCommandExtractor();

  @Test
  @DisplayName("Cognito identities claim에서 Google 가입 provider를 추출한다")
  void extractsGoogleProvider() {
    Jwt jwt = jwtWithIdentities(List.of(Map.of("providerName", "Google")));

    CompleteRegistrationCommand command =
        extractor.extractRegistration(jwt, UserRole.BUYER, "010-1234-5678");

    assertEquals(SignupProvider.GOOGLE, command.signupProvider());
    assertEquals("010-1234-5678", command.phoneNumber());
  }

  @Test
  @DisplayName("문자열 identities claim에서 Kakao 가입 provider를 추출한다")
  void extractsKakaoProviderFromStringClaim() {
    Jwt jwt = jwtWithIdentities("[{\"providerName\":\"Kakao\"}]");

    CompleteRegistrationCommand command =
        extractor.extractRegistration(jwt, UserRole.SELLER, "010-1234-5678");

    assertEquals(SignupProvider.KAKAO, command.signupProvider());
  }

  @Test
  @DisplayName("가입 provider를 식별할 수 없으면 등록을 거절한다")
  void rejectsMissingProvider() {
    Jwt jwt = jwtWithIdentities(List.of());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> extractor.extractRegistration(jwt, UserRole.BUYER, "010-1234-5678"));

    assertEquals(CommonErrorCode.UNAUTHORIZED, exception.getErrorCode());
  }

  private Jwt jwtWithIdentities(Object identities) {
    Instant now = Instant.now();
    return Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("cognito-sub")
        .claim("email", "user@example.test")
        .claim("name", "사용자")
        .claim("identities", identities)
        .issuedAt(now)
        .expiresAt(now.plusSeconds(3600))
        .build();
  }
}
