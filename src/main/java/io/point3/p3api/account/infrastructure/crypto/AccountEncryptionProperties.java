package io.point3.p3api.account.infrastructure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "p3.account.encryption")
public record AccountEncryptionProperties(String key, String keyVersion) {

  private static final String DEFAULT_KEY_VERSION = "v1";

  public AccountEncryptionProperties {
    key = key == null ? "" : key.trim();
    keyVersion =
        keyVersion == null || keyVersion.isBlank() ? DEFAULT_KEY_VERSION : keyVersion.trim();
  }
}
