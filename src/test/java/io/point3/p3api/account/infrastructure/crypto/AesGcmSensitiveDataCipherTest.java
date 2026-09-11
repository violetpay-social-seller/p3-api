package io.point3.p3api.account.infrastructure.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AesGcmSensitiveDataCipherTest {

  private static final String KEY = Base64.getEncoder()
      .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

  private final AesGcmSensitiveDataCipher cipher =
      new AesGcmSensitiveDataCipher(new AccountEncryptionProperties(KEY, "v1"));

  @Test
  @DisplayName("민감정보를 AES-GCM으로 암호화하고 복호화한다")
  void encryptsAndDecrypts() {
    String plaintext = "123456789012";

    String encrypted = cipher.encrypt(plaintext);

    assertNotEquals(plaintext, encrypted);
    assertEquals(plaintext, cipher.decrypt(encrypted));
  }

  @Test
  @DisplayName("동일한 값도 매번 다른 IV로 암호화한다")
  void usesRandomIv() {
    String first = cipher.encrypt("123456789012");
    String second = cipher.encrypt("123456789012");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("암호화 키가 없거나 형식이 잘못되면 처리하지 않는다")
  void rejectsInvalidKey() {
    AesGcmSensitiveDataCipher missingKey =
        new AesGcmSensitiveDataCipher(new AccountEncryptionProperties("", "v1"));
    AesGcmSensitiveDataCipher shortKey =
        new AesGcmSensitiveDataCipher(new AccountEncryptionProperties("c2hvcnQ=", "v1"));

    assertThrows(IllegalStateException.class, () -> missingKey.encrypt("sensitive"));
    assertThrows(IllegalStateException.class, () -> shortKey.encrypt("sensitive"));
  }

  @Test
  @DisplayName("변조된 암호문은 복호화하지 않는다")
  void rejectsTamperedValue() {
    String encrypted = cipher.encrypt("123456789012");
    String[] parts = encrypted.split("\\.");
    char replacement = parts[2].charAt(0) == 'A' ? 'B' : 'A';
    String tampered = parts[0] + "." + parts[1] + "." + replacement + parts[2].substring(1);

    assertThrows(IllegalArgumentException.class, () -> cipher.decrypt(tampered));
  }
}
