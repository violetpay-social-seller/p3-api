package io.point3.p3api.user.domain.type;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SignupProvider {
  GOOGLE("Google"),
  KAKAO("Kakao");

  private final String cognitoProviderName;

  public static SignupProvider fromCognitoProviderName(String providerName) {
    return Arrays.stream(values())
        .filter(provider -> provider.matches(providerName))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported signup provider"));
  }

  private boolean matches(String providerName) {
    return name().equalsIgnoreCase(providerName)
        || cognitoProviderName.equalsIgnoreCase(providerName);
  }
}
