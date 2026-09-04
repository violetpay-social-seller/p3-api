package io.point3.p3api.asset.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssetDeliveryUrlResolverTest {

  @Test
  @DisplayName("delivery base URL의 trailing slash를 제거하고 object key를 연결한다")
  void normalizesTrailingSlash() {
    AssetDeliveryUrlResolver resolver =
        new AssetDeliveryUrlResolver("https://assets.example.test///");

    assertEquals(
        "https://assets.example.test/assets/example.webp", resolver.resolve("assets/example.webp"));
  }

  @Test
  @DisplayName("delivery base URL이 비어 있으면 URL을 만들지 않는다")
  void returnsNullWhenBaseUrlIsBlank() {
    AssetDeliveryUrlResolver resolver = new AssetDeliveryUrlResolver(" ");

    assertNull(resolver.resolve("assets/example.webp"));
  }
}
