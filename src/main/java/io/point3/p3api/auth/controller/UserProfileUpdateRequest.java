package io.point3.p3api.auth.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.UUID;

@JsonDeserialize(using = UserProfileUpdateRequest.Deserializer.class)
public record UserProfileUpdateRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(max = 100) String name,
    UUID profileAssetId,
    @JsonIgnore boolean profileAssetIdProvided) {

  static final class Deserializer extends JsonDeserializer<UserProfileUpdateRequest> {

    @Override
    public UserProfileUpdateRequest deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      ObjectMapper mapper = (ObjectMapper) parser.getCodec();
      JsonNode node = mapper.readTree(parser);
      boolean profileAssetIdProvided = node.has("profileAssetId");

      return new UserProfileUpdateRequest(
          read(mapper, node, "email", String.class),
          read(mapper, node, "name", String.class),
          read(mapper, node, "profileAssetId", UUID.class),
          profileAssetIdProvided);
    }

    private static <T> T read(ObjectMapper mapper, JsonNode node, String fieldName, Class<T> type)
        throws IOException {
      JsonNode value = node.get(fieldName);
      if (value == null || value.isNull()) {
        return null;
      }
      return mapper.treeToValue(value, type);
    }
  }
}
