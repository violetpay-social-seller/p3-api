package io.point3.p3api.payment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.point3.p3api.payment.config.Point3Properties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Point3AuthenticationUrlFactoryTest {

  @Test
  @DisplayName("Point3 인증 URL은 widget redirect와 sessionId를 포함한다")
  void buildsAuthenticationUrl() {
    Point3Properties properties = new Point3Properties(
        "https://api.point3.test/",
        "https://auth.point3.test/",
        "https://widget.point3.test/",
        "test-client",
        "test-token",
        Duration.ofHours(1));

    String authenticationUrl =
        Point3AuthenticationUrlFactory.build(properties, "pymt_sess-123", null);

    assertEquals(
        "https://auth.point3.test/?client_id=test-client&redirect_uri="
            + "https%3A%2F%2Fwidget.point3.test%2F&sessionId=pymt_sess-123",
        authenticationUrl);
    assertFalse(authenticationUrl.contains("session_id="));
    assertFalse(authenticationUrl.contains("state="));
  }

  @Test
  @DisplayName("저장된 payerId가 있으면 인증 URL에 payer_id를 포함한다")
  void buildsAuthenticationUrlWithPayerId() {
    Point3Properties properties = new Point3Properties(
        "https://api.point3.test",
        "https://auth.point3.test",
        "https://widget.point3.test",
        "test-client",
        "test-token",
        Duration.ofHours(1));

    String authenticationUrl =
        Point3AuthenticationUrlFactory.build(properties, "pymt_sess-123", "payer-saved");

    assertEquals(
        "https://auth.point3.test/?client_id=test-client&redirect_uri="
            + "https%3A%2F%2Fwidget.point3.test%2F&sessionId=pymt_sess-123"
            + "&payer_id=payer-saved",
        authenticationUrl);
  }
}
