package io.point3.p3api.asset.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AssetDeliveryUrlResolver {

  private final String deliveryBaseUrl;

  public AssetDeliveryUrlResolver(@Value("${p3.asset.delivery.base-url:}") String deliveryBaseUrl) {
    this.deliveryBaseUrl = normalize(deliveryBaseUrl);
  }

  public String resolve(String objectKey) {
    if (deliveryBaseUrl == null || deliveryBaseUrl.isBlank()) {
      return null;
    }
    return deliveryBaseUrl + "/" + objectKey;
  }

  private String normalize(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return null;
    }
    return baseUrl.replaceAll("/+$", "");
  }
}
