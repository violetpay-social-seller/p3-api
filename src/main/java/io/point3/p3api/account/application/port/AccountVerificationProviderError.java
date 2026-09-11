package io.point3.p3api.account.application.port;

import java.util.LinkedHashMap;
import java.util.Map;

public record AccountVerificationProviderError(
    Integer httpStatus,
    String responseCode,
    String responseMessage,
    String bankResponseCode,
    String bankResponseMessage) {

  public AccountVerificationProviderError {
    responseCode = normalize(responseCode);
    responseMessage = normalize(responseMessage);
    bankResponseCode = normalize(bankResponseCode);
    bankResponseMessage = normalize(bankResponseMessage);
  }

  public static AccountVerificationProviderError httpStatus(int httpStatus) {
    return new AccountVerificationProviderError(httpStatus, null, null, null, null);
  }

  public String primaryCode() {
    if (isApiSuccess()) {
      return bankResponseCode;
    }
    if (responseCode != null) {
      return responseCode;
    }
    if (bankResponseCode != null) {
      return bankResponseCode;
    }
    return httpStatus == null ? null : String.valueOf(httpStatus);
  }

  public String primaryMessage() {
    if (isApiSuccess()) {
      return bankResponseMessage;
    }
    if (responseMessage != null) {
      return responseMessage;
    }
    return bankResponseMessage;
  }

  public boolean hasProviderValue() {
    return httpStatus != null
        || responseCode != null
        || responseMessage != null
        || bankResponseCode != null
        || bankResponseMessage != null;
  }

  public Map<String, Object> toMetadata() {
    Map<String, Object> provider = new LinkedHashMap<>();
    provider.put("name", "KFTC_OPEN_BANKING");
    putIfPresent(provider, "httpStatus", httpStatus);
    putIfPresent(provider, "responseCode", responseCode);
    putIfPresent(provider, "responseMessage", responseMessage);
    putIfPresent(provider, "bankResponseCode", bankResponseCode);
    putIfPresent(provider, "bankResponseMessage", bankResponseMessage);
    return Map.of("provider", Map.copyOf(provider));
  }

  private static void putIfPresent(Map<String, Object> values, String key, Object value) {
    if (value != null) {
      values.put(key, value);
    }
  }

  private static String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private boolean isApiSuccess() {
    return "A0000".equals(responseCode);
  }
}
