package io.point3.p3api.inquiry.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@JsonDeserialize(using = CreateOrderFormDraftRequest.Deserializer.class)
public record CreateOrderFormDraftRequest(
    @NotNull UUID orderFormTemplateId,
    @NotNull LocalDate pickupDate,
    @NotNull LocalTime pickupTime,
    boolean noticeAgreed,
    boolean cancellationRefundAgreed,
    @Valid @NotNull List<FormAnswer> formAnswers,
    @Valid ReferenceAsset startReferenceAsset,
    @JsonIgnore boolean startReferenceAssetProvided) {

  public CreateOrderFormDraftRequest {
    formAnswers = formAnswers == null ? null : List.copyOf(formAnswers);
  }

  @Override
  public List<FormAnswer> formAnswers() {
    return formAnswers == null ? null : List.copyOf(formAnswers);
  }

  public record FormAnswer(
      @NotNull UUID optionGroupId, @NotNull Object value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId, @NotNull OrderFormReferenceAssetSource source) {}

  static final class Deserializer extends JsonDeserializer<CreateOrderFormDraftRequest> {

    private static final TypeReference<List<FormAnswer>> FORM_ANSWERS = new TypeReference<>() {};

    @Override
    public CreateOrderFormDraftRequest deserialize(
        JsonParser parser, DeserializationContext context) throws IOException {
      ObjectMapper mapper = (ObjectMapper) parser.getCodec();
      JsonNode node = mapper.readTree(parser);
      boolean startReferenceAssetProvided = node.has("startReferenceAsset");

      return new CreateOrderFormDraftRequest(
          read(mapper, node, "orderFormTemplateId", UUID.class),
          read(mapper, node, "pickupDate", LocalDate.class),
          read(mapper, node, "pickupTime", LocalTime.class),
          readBoolean(node, "noticeAgreed"),
          readBoolean(node, "cancellationRefundAgreed"),
          readFormAnswers(mapper, node),
          readStartReferenceAsset(mapper, node, startReferenceAssetProvided),
          startReferenceAssetProvided);
    }

    private static <T> T read(ObjectMapper mapper, JsonNode node, String fieldName, Class<T> type)
        throws IOException {
      JsonNode value = node.get(fieldName);
      if (value == null || value.isNull()) {
        return null;
      }
      return mapper.treeToValue(value, type);
    }

    private static boolean readBoolean(JsonNode node, String fieldName) {
      JsonNode value = node.get(fieldName);
      return value != null && value.asBoolean(false);
    }

    private static List<FormAnswer> readFormAnswers(ObjectMapper mapper, JsonNode node) {
      JsonNode value = node.get("formAnswers");
      if (value == null || value.isNull()) {
        return null;
      }
      return mapper.convertValue(value, FORM_ANSWERS);
    }

    private static ReferenceAsset readStartReferenceAsset(
        ObjectMapper mapper, JsonNode node, boolean provided) throws IOException {
      if (!provided || node.get("startReferenceAsset").isNull()) {
        return null;
      }
      return mapper.treeToValue(node.get("startReferenceAsset"), ReferenceAsset.class);
    }
  }
}
