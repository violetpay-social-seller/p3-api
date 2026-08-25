package io.point3.p3api.local;

import io.point3.p3api.asset.application.port.AssetStoragePort;
import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-scenario")
public class LocalScenarioAssetStorage implements AssetStoragePort {

  private final Path storageRoot;

  public LocalScenarioAssetStorage(
      @Value("${p3.local-scenario.asset-storage-root}") String storageRoot) {
    this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
  }

  @Override
  public void store(StoreAssetCommand command) {
    Path target = storageRoot.resolve(command.objectKey()).normalize();
    if (!target.startsWith(storageRoot)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid asset storage key");
    }

    try {
      Path parent = target.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.copy(command.inputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Failed to store local asset");
    }
  }
}
