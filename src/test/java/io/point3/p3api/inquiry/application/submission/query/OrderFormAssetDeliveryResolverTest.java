package io.point3.p3api.inquiry.application.submission.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.inquiry.application.submission.result.OrderFormAssetDelivery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderFormAssetDeliveryResolverTest {

  private final AssetPersistencePort assetPersistencePort =
      Mockito.mock(AssetPersistencePort.class);
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver =
      Mockito.mock(AssetDeliveryUrlResolver.class);
  private final AssetVariantDeliveryService assetVariantDeliveryService =
      Mockito.mock(AssetVariantDeliveryService.class);
  private final OrderFormAssetDeliveryResolver resolver = new OrderFormAssetDeliveryResolver(
      assetPersistencePort, assetDeliveryUrlResolver, assetVariantDeliveryService);

  @Test
  void fallsBackToOriginalAssetDeliveryBeforeVariantReady() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    Asset asset = Asset.create(
        assetId,
        UUID.randomUUID(),
        "reference.png",
        "image/png",
        1024,
        "original/11111111-1111-4111-8111-111111111111/reference.png");
    when(assetPersistencePort.findAllById(List.of(assetId))).thenReturn(List.of(asset));
    when(assetDeliveryUrlResolver.resolve(asset.getObjectKey()))
        .thenReturn("https://assets.example.test/original/reference.png");
    when(assetVariantDeliveryService.resolveReadyDeliveries(List.of(assetId)))
        .thenReturn(Map.of(assetId, AssetVariantDelivery.empty()));

    OrderFormAssetDelivery delivery = resolver.resolve(List.of(assetId)).get(assetId);

    assertEquals("UPLOADED", delivery.status());
    assertEquals("https://assets.example.test/original/reference.png", delivery.deliveryUrl());
    assertTrue(delivery.variants().isEmpty());
  }

  @Test
  void prefersReadyVariantDelivery() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    Asset asset = Asset.create(
        assetId,
        UUID.randomUUID(),
        "reference.png",
        "image/png",
        1024,
        "original/11111111-1111-4111-8111-111111111111/reference.png");
    AssetVariantDelivery variantDelivery = new AssetVariantDelivery(
        "https://assets.example.test/processed/reference_640.webp",
        List.of(new AssetVariantDelivery.Variant(
            "MEDIUM", "https://assets.example.test/processed/reference_640.webp", 640, 480)));
    when(assetPersistencePort.findAllById(List.of(assetId))).thenReturn(List.of(asset));
    when(assetDeliveryUrlResolver.resolve(asset.getObjectKey()))
        .thenReturn("https://assets.example.test/original/reference.png");
    when(assetVariantDeliveryService.resolveReadyDeliveries(List.of(assetId)))
        .thenReturn(Map.of(assetId, variantDelivery));

    OrderFormAssetDelivery delivery = resolver.resolve(List.of(assetId)).get(assetId);

    assertEquals("UPLOADED", delivery.status());
    assertEquals("https://assets.example.test/processed/reference_640.webp", delivery.deliveryUrl());
    assertEquals(1, delivery.variants().size());
  }

  @Test
  void reportsMissingAsset() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    when(assetPersistencePort.findAllById(List.of(assetId))).thenReturn(List.of());
    when(assetVariantDeliveryService.resolveReadyDeliveries(List.of(assetId))).thenReturn(Map.of());

    OrderFormAssetDelivery delivery = resolver.resolve(List.of(assetId)).get(assetId);

    assertEquals("MISSING", delivery.status());
    assertNull(delivery.deliveryUrl());
    assertTrue(delivery.variants().isEmpty());
  }
}
