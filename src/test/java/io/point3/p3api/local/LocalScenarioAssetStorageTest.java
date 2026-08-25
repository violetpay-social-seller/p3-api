package io.point3.p3api.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.exception.BaseException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalScenarioAssetStorageTest {

  @TempDir
  Path tempDir;

  @Test
  void 로컬_시나리오_자산은_설정된_루트_아래에_저장된다() throws Exception {
    LocalScenarioAssetStorage storage = new LocalScenarioAssetStorage(tempDir.toString());

    storage.store(command("originals/test.png", "fixture"));

    assertThat(Files.readString(tempDir.resolve("originals/test.png"))).isEqualTo("fixture");
  }

  @Test
  void 로컬_시나리오_자산_저장은_루트_밖_경로를_거절한다() {
    LocalScenarioAssetStorage storage = new LocalScenarioAssetStorage(tempDir.toString());

    assertThatThrownBy(() -> storage.store(command("../escape.png", "fixture")))
        .isInstanceOf(BaseException.class);
  }

  private StoreAssetCommand command(String objectKey, String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    return new StoreAssetCommand(
        new ByteArrayInputStream(bytes), objectKey, "image/png", bytes.length);
  }
}
