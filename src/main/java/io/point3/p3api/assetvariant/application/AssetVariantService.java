package io.point3.p3api.assetvariant.application;

import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.port.AssetVariantPersistencePort;
import io.point3.p3api.assetvariant.application.query.AssetVariantQueryUseCase;
import io.point3.p3api.assetvariant.application.register.AssetVariantRegisterUseCase;
import io.point3.p3api.assetvariant.application.register.RegisterAssetVariantsCommand;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssetVariantService implements AssetVariantRegisterUseCase, AssetVariantQueryUseCase {

  private final AssetPersistencePort assetPersistencePort;
  private final AssetVariantPersistencePort assetVariantPersistencePort;
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver;

  public AssetVariantService(
      AssetPersistencePort assetPersistencePort,
      AssetVariantPersistencePort assetVariantPersistencePort,
      AssetDeliveryUrlResolver assetDeliveryUrlResolver) {
    this.assetPersistencePort = assetPersistencePort;
    this.assetVariantPersistencePort = assetVariantPersistencePort;
    this.assetDeliveryUrlResolver = assetDeliveryUrlResolver;
  }

  @Override
  public RegisteredAssetVariants register(RegisterAssetVariantsCommand command) {
    Asset asset = assetPersistencePort
        .findById(command.assetId())
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));

    validateNotRegistered(command);
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
        command.assetId(), registeredVariants, assetDeliveryUrlResolver::resolve);
  }

  @Override
  @Transactional(readOnly = true)
  public RegisteredAssetVariants getVariants(java.util.UUID assetId) {
    if (assetPersistencePort.findById(assetId).isEmpty()) {
      throw new BaseException(CommonErrorCode.INVALID_ID);
    }
    return RegisteredAssetVariants.from(
        assetId,
        assetVariantPersistencePort.findAllByAssetId(assetId),
        assetDeliveryUrlResolver::resolve);
  }

  private void validateNotRegistered(RegisterAssetVariantsCommand command) {
    boolean exists = command.variants().stream()
        .anyMatch(variant ->
            assetVariantPersistencePort.existsByAssetIdAndType(command.assetId(), variant.type()));
    if (exists) {
      throw new BaseException(AssetErrorCode.ASSET_VARIANT_ALREADY_EXISTS);
    }
  }
}
