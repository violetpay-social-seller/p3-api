package io.point3.p3api.assetvariant.application;

import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.assetvariant.application.port.AssetVariantPersistencePort;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantStatus;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetVariantDeliveryService {

  private final AssetVariantPersistencePort assetVariantPersistencePort;
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver;

  public AssetVariantDelivery resolveReadyDelivery(UUID assetId) {
    return toDelivery(assetVariantPersistencePort.findAllByAssetId(assetId));
  }

  public Map<UUID, AssetVariantDelivery> resolveReadyDeliveries(List<UUID> assetIds) {
    if (assetIds.isEmpty()) {
      return Map.of();
    }

    Map<UUID, List<AssetVariant>> variantsByAssetId =
        assetVariantPersistencePort.findAllByAssetIds(assetIds).stream()
            .collect(Collectors.groupingBy(variant -> variant.getAsset().getId()));

    return assetIds.stream()
        .distinct()
        .collect(Collectors.toMap(
            assetId -> assetId,
            assetId -> toDelivery(variantsByAssetId.getOrDefault(assetId, List.of()))));
  }

  public void validateReadyDelivery(UUID assetId) {
    if (!assetVariantPersistencePort.existsByAssetIdAndStatus(assetId, AssetVariantStatus.READY)) {
      throw new BaseException(AssetErrorCode.ASSET_VARIANT_NOT_READY);
    }
  }

  private AssetVariantDelivery toDelivery(List<AssetVariant> variants) {
    String deliveryUrl = variants.stream()
        .filter(variant -> variant.getStatus() == AssetVariantStatus.READY)
        .min(Comparator.comparingInt(this::variantPriority))
        .map(AssetVariant::getObjectKey)
        .map(assetDeliveryUrlResolver::resolve)
        .orElse(null);

    List<AssetVariantDelivery.Variant> readyVariants = variants.stream()
        .filter(variant -> variant.getStatus() == AssetVariantStatus.READY)
        .sorted(Comparator.comparingInt(AssetVariant::getWidth))
        .map(variant -> new AssetVariantDelivery.Variant(
            variant.getType().name(),
            assetDeliveryUrlResolver.resolve(variant.getObjectKey()),
            variant.getWidth(),
            variant.getHeight()))
        .toList();

    return new AssetVariantDelivery(deliveryUrl, readyVariants);
  }

  private int variantPriority(AssetVariant variant) {
    if (variant.getType() == AssetVariantType.MEDIUM) {
      return 0;
    }
    if (variant.getType() == AssetVariantType.LARGE) {
      return 1;
    }
    return 2;
  }
}
