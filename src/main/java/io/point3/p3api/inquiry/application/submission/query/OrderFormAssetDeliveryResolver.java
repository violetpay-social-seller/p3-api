package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.inquiry.application.submission.result.OrderFormAssetDelivery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderFormAssetDeliveryResolver {

  private final AssetPersistencePort assetPersistencePort;
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver;
  private final AssetVariantDeliveryService assetVariantDeliveryService;

  Map<UUID, OrderFormAssetDelivery> resolve(List<UUID> assetIds) {
    if (assetIds.isEmpty()) {
      return Map.of();
    }

    List<UUID> distinctAssetIds = assetIds.stream().distinct().toList();
    Map<UUID, Asset> assetsById = assetPersistencePort.findAllById(distinctAssetIds).stream()
        .collect(Collectors.toMap(Asset::getId, Function.identity()));
    Map<UUID, AssetVariantDelivery> variantDeliveryByAssetId =
        assetVariantDeliveryService.resolveReadyDeliveries(distinctAssetIds);

    return distinctAssetIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            assetId -> resolveOne(
                assetsById.get(assetId),
                variantDeliveryByAssetId.getOrDefault(assetId, AssetVariantDelivery.empty()))));
  }

  private OrderFormAssetDelivery resolveOne(Asset asset, AssetVariantDelivery variantDelivery) {
    if (asset == null) {
      return OrderFormAssetDelivery.missing();
    }

    return OrderFormAssetDelivery.from(
        asset.getStatus().name(),
        assetDeliveryUrlResolver.resolve(asset.getObjectKey()),
        variantDelivery);
  }
}
