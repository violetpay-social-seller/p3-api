package io.point3.p3api.payment.application;

import io.point3.p3api.payment.config.Point3Properties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class Point3AuthenticationUrlFactory {

  private Point3AuthenticationUrlFactory() {}

  static String build(Point3Properties point3Properties, String sessionId, String payerId) {
    StringBuilder url = new StringBuilder(point3Properties.authBaseUrl())
        .append("/")
        .append("?client_id=")
        .append(encode(point3Properties.clientId()))
        .append("&redirect_uri=")
        .append(encode(point3Properties.paymentOrigin() + "/"))
        .append("&sessionId=")
        .append(encode(sessionId));

    if (payerId != null) {
      url.append("&payer_id=").append(encode(payerId));
    }

    return url.toString();
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
