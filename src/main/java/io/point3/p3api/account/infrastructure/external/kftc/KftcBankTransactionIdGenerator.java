package io.point3.p3api.account.infrastructure.external.kftc;

import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class KftcBankTransactionIdGenerator {

  private static final char[] CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final int UNIQUE_PART_LENGTH = 9;

  private final KftcProperties properties;
  private final SecureRandom random;

  public KftcBankTransactionIdGenerator(KftcProperties properties) {
    this(properties, new SecureRandom());
  }

  KftcBankTransactionIdGenerator(KftcProperties properties, SecureRandom random) {
    this.properties = properties;
    this.random = random;
  }

  public String generate() {
    try {
      properties.validateAccountInquiry();
    } catch (KftcConfigurationException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.CONFIGURATION, exception);
    }

    StringBuilder uniquePart = new StringBuilder(UNIQUE_PART_LENGTH);
    for (int index = 0; index < UNIQUE_PART_LENGTH; index++) {
      uniquePart.append(CHARACTERS[random.nextInt(CHARACTERS.length)]);
    }
    return properties.useOrgCode() + "U" + uniquePart;
  }
}
