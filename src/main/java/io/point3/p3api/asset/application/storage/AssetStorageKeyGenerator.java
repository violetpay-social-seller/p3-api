package io.point3.p3api.asset.application.storage;

import java.text.Normalizer;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetStorageKeyGenerator {

  public static String original(UUID assetId, String originalFileName) {
    String safeFileName = sanitize(originalFileName);
    return "assets/%s/original/%s".formatted(assetId, safeFileName);
  }

  private static String sanitize(String filename) {
    if (filename == null || filename.isBlank()) {
      return "unnamed";
    }

    String normalized = Normalizer.normalize(filename, Normalizer.Form.NFC);

    return normalized
        .replaceAll("[\\\\/]", "_")
        .replaceAll("[\\r\\n\\t]", "_")
        .replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
  }
}
