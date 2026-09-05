package io.point3.p3api.inquiry.application.submission.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFormAnswerDeliveryService {

  private final ObjectMapper objectMapper;
  private final AssetVariantDeliveryService assetVariantDeliveryService;

  public String appendImageDeliveries(String answers) {
    JsonNode root = readAnswers(answers);
    if (!root.isArray()) {
      return answers;
    }

    Set<UUID> assetIds = collectAssetIds(root);
    if (assetIds.isEmpty()) {
      return answers;
    }

    Map<UUID, AssetVariantDelivery> deliveryByAssetId =
        assetVariantDeliveryService.resolveReadyDeliveries(assetIds.stream().toList());
    appendAssets(root, deliveryByAssetId);
    return writeAnswers(root);
  }

  private JsonNode readAnswers(String answers) {
    try {
      return objectMapper.readTree(answers);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private String writeAnswers(JsonNode root) {
    try {
      return objectMapper.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Set<UUID> collectAssetIds(JsonNode root) {
    Set<UUID> assetIds = new LinkedHashSet<>();
    root.forEach(answer -> {
      collectAssetIdsFromOptions(answer.get("value"), assetIds);
      collectAssetIdsFromOptions(answer.get("selectedOptions"), assetIds);
    });
    return assetIds;
  }

  private void collectAssetIdsFromOptions(JsonNode options, Set<UUID> assetIds) {
    if (options == null || !options.isArray()) {
      return;
    }

    options.forEach(option -> assetIdTexts(option).stream()
        .map(this::parseAssetId)
        .flatMap(Optional::stream)
        .forEach(assetIds::add));
  }

  private void appendAssets(JsonNode root, Map<UUID, AssetVariantDelivery> deliveryByAssetId) {
    root.forEach(answer -> {
      appendAssetsToOptions(answer.get("value"), deliveryByAssetId);
      appendAssetsToOptions(answer.get("selectedOptions"), deliveryByAssetId);
    });
  }

  private void appendAssetsToOptions(
      JsonNode options, Map<UUID, AssetVariantDelivery> deliveryByAssetId) {
    if (options == null || !options.isArray()) {
      return;
    }

    options
        .valueStream()
        .filter(ObjectNode.class::isInstance)
        .map(ObjectNode.class::cast)
        .forEach(option -> appendAssets(option, deliveryByAssetId));
  }

  private void appendAssets(ObjectNode option, Map<UUID, AssetVariantDelivery> deliveryByAssetId) {
    List<String> assetIdTexts = assetIdTexts(option);
    if (assetIdTexts.isEmpty()) {
      return;
    }

    ArrayNode assets = objectMapper.createArrayNode();
    assetIdTexts.forEach(assetIdText -> assets.add(asset(assetIdText, deliveryByAssetId)));
    option.set("assets", assets);
  }

  private ObjectNode asset(String assetIdText, Map<UUID, AssetVariantDelivery> deliveryByAssetId) {
    AssetVariantDelivery delivery = parseAssetId(assetIdText)
        .map(assetId -> deliveryByAssetId.getOrDefault(assetId, AssetVariantDelivery.empty()))
        .orElseGet(AssetVariantDelivery::empty);

    ObjectNode asset = objectMapper.createObjectNode();
    asset.put("assetId", assetIdText);
    if (delivery.deliveryUrl() == null) {
      asset.putNull("deliveryUrl");
    } else {
      asset.put("deliveryUrl", delivery.deliveryUrl());
    }
    asset.set("variants", variants(delivery));
    return asset;
  }

  private ArrayNode variants(AssetVariantDelivery delivery) {
    ArrayNode variants = objectMapper.createArrayNode();
    delivery.variants().forEach(variant -> {
      ObjectNode item = objectMapper.createObjectNode();
      item.put("type", variant.type());
      item.put("deliveryUrl", variant.deliveryUrl());
      item.put("width", variant.width());
      item.put("height", variant.height());
      variants.add(item);
    });
    return variants;
  }

  private List<String> assetIdTexts(JsonNode option) {
    JsonNode assetIds = option == null ? null : option.get("assetIds");
    if (assetIds == null || !assetIds.isArray()) {
      return List.of();
    }

    return assetIds
        .valueStream()
        .filter(JsonNode::isTextual)
        .map(JsonNode::asText)
        .toList();
  }

  private Optional<UUID> parseAssetId(String assetId) {
    try {
      return Optional.of(UUID.fromString(assetId));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
