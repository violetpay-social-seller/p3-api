package io.point3.p3api.assetvariant.application;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.port.AssetVariantPersistencePort;
import io.point3.p3api.assetvariant.application.register.AssetVariantRegisterUseCase;
import io.point3.p3api.assetvariant.application.register.RegisterAssetVariantsCommand;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssetVariantService implements AssetVariantRegisterUseCase {

  private final AssetPersistencePort assetPersistencePort;
  private final AssetVariantPersistencePort assetVariantPersistencePort;
  private final String deliveryBaseUrl;

  public AssetVariantService(
      AssetPersistencePort assetPersistencePort,
      AssetVariantPersistencePort assetVariantPersistencePort,
      @Value("${p3.asset.delivery.base-url:}") String deliveryBaseUrl) {
    this.assetPersistencePort = assetPersistencePort;
    this.assetVariantPersistencePort = assetVariantPersistencePort;
    this.deliveryBaseUrl = deliveryBaseUrl;
  }

  @Override
  public RegisteredAssetVariants register(RegisterAssetVariantsCommand command) {
    Asset asset = assetPersistencePort
        .findById(command.assetId())
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));

    List<AssetVariant> variants = command.variants().stream()
        .map(variant -> AssetVariant.create(
            asset,
            variant.type(),
            variant.objectKey(),
            variant.contentType(),
            variant.width(),
            variant.height(),
            variant.sizeBytes()))
        .toList();

    List<AssetVariant> registeredVariants = assetVariantPersistencePort.saveAll(variants);
    return RegisteredAssetVariants.from(
        command.assetId(), registeredVariants, this::resolveDeliveryUrl);
  }

  private String resolveDeliveryUrl(String objectKey) {
    if (deliveryBaseUrl == null || deliveryBaseUrl.isBlank()) {
      return null;
    }
    return deliveryBaseUrl + "/" + objectKey;
  }
}
