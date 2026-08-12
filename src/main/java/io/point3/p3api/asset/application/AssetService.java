package io.point3.p3api.asset.application;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.application.port.AssetStoragePort;
import io.point3.p3api.asset.application.register.AssetRegisterUseCase;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.asset.application.result.RegistryAsset;
import io.point3.p3api.asset.application.storage.AssetStorageKeyGenerator;
import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.asset.domain.entity.Asset;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssetService implements AssetRegisterUseCase {

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

  private String resolveDeliveryUrl(String storageKey) {
    if (deliveryBaseUrl == null || deliveryBaseUrl.isBlank()) {
      return null;
    }
    return deliveryBaseUrl + "/" + storageKey;
  }
}
