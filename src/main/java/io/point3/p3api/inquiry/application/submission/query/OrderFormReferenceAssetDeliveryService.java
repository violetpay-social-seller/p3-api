package io.point3.p3api.inquiry.application.submission.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.submission.result.OrderFormReferenceAssetResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFormReferenceAssetDeliveryService {

  private final ObjectMapper objectMapper;
  private final AssetVariantDeliveryService assetVariantDeliveryService;

  public List<OrderFormReferenceAssetResult> appendDeliveries(String referenceAssets) {
    if (referenceAssets == null || referenceAssets.isBlank()) {
      return List.of();
    }

    List<ReferenceAssetSnapshot> snapshots = readSnapshots(referenceAssets);
    if (snapshots.isEmpty()) {
      return List.of();
    }

    Map<UUID, AssetVariantDelivery> deliveries = assetVariantDeliveryService.resolveReadyDeliveries(
        snapshots.stream().map(ReferenceAssetSnapshot::assetId).toList());
    return snapshots.stream()
        .sorted(Comparator.comparingInt(ReferenceAssetSnapshot::sortOrder))
        .map(snapshot -> OrderFormReferenceAssetResult.of(
            snapshot.assetId(),
            snapshot.source(),
            snapshot.sortOrder(),
            deliveries.getOrDefault(snapshot.assetId(), AssetVariantDelivery.empty())))
        .toList();
  }

  private List<ReferenceAssetSnapshot> readSnapshots(String referenceAssets) {
    JsonNode root = read(referenceAssets);
    if (!root.isArray()) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    return IntStream.range(0, root.size())
        .mapToObj(index -> readSnapshot(root.get(index), index))
        .toList();
  }

  private JsonNode read(String referenceAssets) {
    try {
      return objectMapper.readTree(referenceAssets);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private ReferenceAssetSnapshot readSnapshot(JsonNode node, int fallbackSortOrder) {
    if (node.isTextual()) {
      return new ReferenceAssetSnapshot(parseAssetId(node.asText()), null, fallbackSortOrder);
    }

    JsonNode assetId = node.get("assetId");
    JsonNode sortOrder = node.get("sortOrder");
    if (assetId == null
        || !assetId.isTextual()
        || sortOrder == null
        || !sortOrder.canConvertToInt()) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    return new ReferenceAssetSnapshot(
        parseAssetId(assetId.asText()), readSource(node.get("source")), sortOrder.asInt());
  }

  private UUID parseAssetId(String assetId) {
    try {
      return UUID.fromString(assetId);
    } catch (IllegalArgumentException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private OrderFormReferenceAssetSource readSource(JsonNode source) {
    if (source == null || source.isNull()) {
      return null;
    }
    if (!source.isTextual()) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    try {
      return OrderFormReferenceAssetSource.valueOf(source.asText());
    } catch (IllegalArgumentException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private record ReferenceAssetSnapshot(
      UUID assetId, OrderFormReferenceAssetSource source, int sortOrder) {}
}
