package io.point3.p3api.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class LocalScenarioJwtDecoderConfigTest {

  @Test
  void 로컬_시나리오_토큰을_테스트_계정_claim으로_변환한다() {
    JwtDecoder decoder = newDecoder();

    Jwt jwt = decoder.decode("seller-token");

    assertThat(jwt.getSubject()).isEqualTo("seller-sub");
    assertThat(jwt.getClaimAsString("email")).isEqualTo("seller@example.com");
    assertThat(jwt.getClaimAsString("name")).isEqualTo("Seller");
  }

  @Test
  void 로컬_시나리오_운영자_토큰을_claim으로_변환한다() {
    JwtDecoder decoder = newDecoder();

    Jwt jwt = decoder.decode("operator-token");

    assertThat(jwt.getSubject()).isEqualTo("operator-sub");
    assertThat(jwt.getClaimAsString("email")).isEqualTo("operator@example.com");
    assertThat(jwt.getClaimAsString("name")).isEqualTo("Operator");
  }

  @Test
  void 등록되지_않은_로컬_시나리오_토큰은_거절한다() {
    JwtDecoder decoder = newDecoder();

    assertThatThrownBy(() -> decoder.decode("unknown-token")).isInstanceOf(BadJwtException.class);
  }

  private JwtDecoder newDecoder() {
    return new LocalScenarioJwtDecoderConfig()
        .localScenarioJwtDecoder(
            "seller-token",
            "seller-sub",
            "seller@example.com",
            "Seller",
            "buyer-token",
            "buyer-sub",
            "buyer@example.com",
            "Buyer",
            "operator-token",
            "operator-sub",
            "operator@example.com",
            "Operator");
  }
}
