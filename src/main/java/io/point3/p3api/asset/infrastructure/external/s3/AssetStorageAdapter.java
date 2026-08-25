package io.point3.p3api.asset.infrastructure.persistence;

import io.point3.p3api.asset.application.port.AssetStoragePort;
import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.asset.infrastructure.external.s3.AssetStoreRequest;
import io.point3.p3api.asset.infrastructure.external.s3.S3Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!local-scenario")
@Transactional
@RequiredArgsConstructor
public class AssetStorageAdapter implements AssetStoragePort {

  private final S3Storage s3Storage;

  @Override
  public void store(StoreAssetCommand command) {
    s3Storage.store(AssetStoreRequest.from(command));
  }
}
