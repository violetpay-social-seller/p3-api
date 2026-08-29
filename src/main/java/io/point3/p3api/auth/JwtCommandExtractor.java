package io.point3.p3api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtCommandExtractor {

  private final ObjectMapper objectMapper;

  public JwtCommandExtractor() {
    this.objectMapper = new ObjectMapper();
  }

  public SyncCommand extractSync(Jwt jwt) {
    if (jwt == null) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    String cognitoSub = jwt.getSubject();
    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");

    validateNotBlank(cognitoSub, "cognitoSub");
    validateNotBlank(email, "email");
    validateNotBlank(name, "name");

    return SyncCommand.of(cognitoSub, email, name);
  }

  public CompleteRegistrationCommand extractRegistration(
      Jwt jwt, UserRole role, String phoneNumber) {
    if (jwt == null) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    String cognitoSub = jwt.getSubject();
    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");

    validateNotBlank(cognitoSub, "cognitoSub");
    validateNotBlank(email, "email");
    validateNotBlank(name, "name");
    validateNotBlank(phoneNumber, "phoneNumber");

    return CompleteRegistrationCommand.of(
        cognitoSub, email, name, role, phoneNumber, extractSignupProvider(jwt));
  }

  private SignupProvider extractSignupProvider(Jwt jwt) {
    Object identities = jwt.getClaim("identities");
    String providerName = extractProviderName(identities);
    if (isBlank(providerName)) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED, "signupProvider must not be blank");
    }
    try {
      return SignupProvider.fromCognitoProviderName(providerName);
    } catch (IllegalArgumentException exception) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED, "Unsupported signupProvider");
    }
  }

  private String extractProviderName(Object identities) {
    if (identities instanceof String value) {
      return extractProviderName(value);
    }
    if (identities instanceof Iterable<?> values) {
      for (Object value : values) {
        String providerName = extractProviderName(value);
        if (!isBlank(providerName)) {
          return providerName;
        }
      }
    }
    if (identities instanceof Map<?, ?> values) {
      Object providerName = values.get("providerName");
      return providerName instanceof String value ? value : null;
    }
    if (identities instanceof JsonNode node) {
      return extractProviderName(node);
    }
    return null;
  }

  private String extractProviderName(String identities) {
    if (identities.isBlank()) {
      return null;
    }
    try {
      return extractProviderName(objectMapper.readTree(identities));
    } catch (JsonProcessingException exception) {
      return null;
    }
  }

  private String extractProviderName(JsonNode node) {
    if (node.isArray()) {
      for (JsonNode item : node) {
        String providerName = extractProviderName(item);
        if (!isBlank(providerName)) {
          return providerName;
        }
      }
      return null;
    }
    if (!node.isObject()) {
      return null;
    }
    JsonNode providerName = node.get("providerName");
    return providerName != null && providerName.isTextual() ? providerName.asText() : null;
  }

  private void validateNotBlank(String value, String paramName) {
    if (isBlank(value)) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED, paramName + " must not be blank");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
