package io.point3.p3api.account.infrastructure.crypto;

import io.point3.p3api.account.application.port.SensitiveDataCipher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class AesGcmSensitiveDataCipher implements SensitiveDataCipher {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int KEY_LENGTH_BYTES = 32;
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;
  private static final String VALUE_SEPARATOR = ".";

  private final AccountEncryptionProperties properties;
  private final SecureRandom secureRandom;

  public AesGcmSensitiveDataCipher(AccountEncryptionProperties properties) {
    this(properties, new SecureRandom());
  }

  AesGcmSensitiveDataCipher(AccountEncryptionProperties properties, SecureRandom secureRandom) {
    this.properties = Objects.requireNonNull(properties, "properties");
    this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
  }

  @Override
  public String encrypt(String plaintext) {
    Objects.requireNonNull(plaintext, "plaintext");
    if (plaintext.isEmpty()) {
      throw new IllegalArgumentException("Plaintext must not be empty");
    }

    byte[] iv = new byte[IV_LENGTH_BYTES];
    secureRandom.nextBytes(iv);

    try {
      Cipher cipher = cipher(Cipher.ENCRYPT_MODE, properties.keyVersion(), iv);
      byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      return properties.keyVersion()
          + VALUE_SEPARATOR
          + encode(iv)
          + VALUE_SEPARATOR
          + encode(encrypted);
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Sensitive data encryption failed", exception);
    }
  }

  @Override
  public String decrypt(String encryptedValue) {
    Objects.requireNonNull(encryptedValue, "encryptedValue");
    String[] parts = encryptedValue.split("\\.", -1);
    if (parts.length != 3 || !properties.keyVersion().equals(parts[0])) {
      throw new IllegalArgumentException("Unsupported encrypted value format");
    }

    try {
      byte[] iv = decode(parts[1]);
      if (iv.length != IV_LENGTH_BYTES) {
        throw new IllegalArgumentException("Invalid encrypted value IV");
      }
      Cipher cipher = cipher(Cipher.DECRYPT_MODE, parts[0], iv);
      return new String(cipher.doFinal(decode(parts[2])), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException exception) {
      throw new IllegalArgumentException("Sensitive data decryption failed", exception);
    }
  }

  private Cipher cipher(int mode, String keyVersion, byte[] iv) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(
        mode, new SecretKeySpec(key(), ALGORITHM), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
    cipher.updateAAD(keyVersion.getBytes(StandardCharsets.UTF_8));
    return cipher;
  }

  private byte[] key() {
    byte[] key;
    try {
      key = Base64.getDecoder().decode(properties.key());
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("Account encryption key must be valid Base64", exception);
    }
    if (key.length != KEY_LENGTH_BYTES) {
      throw new IllegalStateException("Account encryption key must contain 32 bytes");
    }
    return key;
  }

  private String encode(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  private byte[] decode(String value) {
    return Base64.getUrlDecoder().decode(value);
  }
}
