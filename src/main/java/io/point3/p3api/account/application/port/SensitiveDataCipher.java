package io.point3.p3api.account.application.port;

public interface SensitiveDataCipher {

  String encrypt(String plaintext);

  String decrypt(String encryptedValue);
}
