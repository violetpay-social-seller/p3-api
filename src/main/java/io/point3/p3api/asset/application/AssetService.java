package io.point3.p3api.asset.application;

import io.point3.p3api.asset.application.delete.AssetDeleteUseCase;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.application.port.AssetStoragePort;
import io.point3.p3api.asset.application.query.AssetQueryUseCase;
import io.point3.p3api.asset.application.register.AssetRegisterUseCase;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.asset.application.result.AssetDetailResult;
import io.point3.p3api.asset.application.result.RegistryAsset;
import io.point3.p3api.asset.application.storage.AssetStorageKeyGenerator;
import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssetService implements AssetRegisterUseCase, AssetQueryUseCase, AssetDeleteUseCase {

  private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp");

  private final AssetStoragePort assetStoragePort;
  private final AssetPersistencePort assetPersistencePort;
  private final String deliveryBaseUrl;

  public AssetService(
      AssetStoragePort assetStoragePort,
      AssetPersistencePort assetPersistencePort,
      @Value("${p3.asset.delivery.base-url:}") String deliveryBaseUrl) {
    this.assetStoragePort = assetStoragePort;
    this.assetPersistencePort = assetPersistencePort;
    this.deliveryBaseUrl = deliveryBaseUrl;
  }

  @Override
  public RegistryAsset register(RegisterAssetCommand command) {
    validateUpload(command);
    UUID assetId = UUID.randomUUID();
    String objectKey = AssetStorageKeyGenerator.original(assetId, command.originalFilename());

    Asset asset = Asset.create(
        assetId,
        command.uploadedBy(),
        command.originalFilename(),
        command.contentType(),
        command.sizeBytes(),
        objectKey);

    assetStoragePort.store(StoreAssetCommand.from(command, objectKey));
    // DB저장 실패시 S3 object만 남을수 있음 MVP단계에서는 orphan cleanup 정책으로 나중에 정리
    Asset registeredAsset = assetPersistencePort.save(asset);

    return RegistryAsset.from(registeredAsset, resolveDeliveryUrl(objectKey));
  }

  @Override
  @Transactional(readOnly = true)
  public AssetDetailResult getAsset(UUID assetId, UUID uploadedBy) {
    return toDetail(findOwnedAsset(assetId, uploadedBy));
  }

  @Override
  @Transactional(readOnly = true)
  public List<AssetDetailResult> getMyAssets(UUID uploadedBy) {
    return assetPersistencePort.findAllByUploadedBy(uploadedBy).stream()
        .map(this::toDetail)
        .toList();
  }

  @Override
  public void delete(UUID assetId, UUID uploadedBy) {
    findOwnedAsset(assetId, uploadedBy).delete();
  }

  private Asset findOwnedAsset(UUID assetId, UUID uploadedBy) {
    return assetPersistencePort
        .findByIdAndUploadedBy(assetId, uploadedBy)
        .orElseThrow(() -> new BaseException(AssetErrorCode.ASSET_NOT_FOUND));
  }

  private AssetDetailResult toDetail(Asset asset) {
    return AssetDetailResult.from(asset, resolveDeliveryUrl(asset.getObjectKey()));
  }

  private void validateUpload(RegisterAssetCommand command) {
    if (!ALLOWED_CONTENT_TYPES.contains(command.contentType())) {
      throw new BaseException(AssetErrorCode.ASSET_CONTENT_TYPE_NOT_ALLOWED);
    }
    if (command.sizeBytes() > MAX_SIZE_BYTES) {
      throw new BaseException(AssetErrorCode.ASSET_SIZE_EXCEEDED);
    }
  }

  private String resolveDeliveryUrl(String storageKey) {
    if (deliveryBaseUrl == null || deliveryBaseUrl.isBlank()) {
      return null;
    }
    return deliveryBaseUrl + "/" + storageKey;
  }
}
