package io.point3.p3api.order.application.query.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import io.point3.p3api.order.application.result.OrderReferenceAssetResult;
import io.point3.p3api.order.domain.entity.Order;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReferenceAssetDeliveryService {

  private static final String MISSING_STATUS = "MISSING";

  private final ObjectMapper objectMapper;
  private final AssetPersistencePort assetPersistencePort;
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver;
  private final AssetVariantDeliveryService assetVariantDeliveryService;

  public List<OrderReferenceAssetResult> appendDeliveries(String referenceAssets) {
    if (referenceAssets == null || referenceAssets.isBlank()) {
      return List.of();
    }

    return appendDeliveries(readSnapshots(referenceAssets));
  }

  public Map<UUID, List<OrderReferenceAssetResult>> appendDeliveriesByOrderId(List<Order> orders) {
    if (orders.isEmpty()) {
      return Map.of();
    }

    Map<UUID, List<ReferenceAssetSnapshot>> snapshotsByOrderId = orders.stream()
        .collect(Collectors.toMap(
            Order::getId,
            order -> readSnapshots(order.getStartReferenceAssets()),
            (left, right) -> left,
            LinkedHashMap::new));
    List<ReferenceAssetSnapshot> snapshots =
        snapshotsByOrderId.values().stream().flatMap(List::stream).toList();
    Map<UUID, OrderReferenceAssetDelivery> deliveries = resolveDeliveries(snapshots);

    return snapshotsByOrderId.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream()
                .sorted(Comparator.comparingInt(ReferenceAssetSnapshot::sortOrder))
                .map(snapshot -> toResult(snapshot, deliveries))
                .toList(),
            (left, right) -> left,
            LinkedHashMap::new));
  }

  private List<OrderReferenceAssetResult> appendDeliveries(List<ReferenceAssetSnapshot> snapshots) {
    Map<UUID, OrderReferenceAssetDelivery> deliveries = resolveDeliveries(snapshots);
    return snapshots.stream()
        .sorted(Comparator.comparingInt(ReferenceAssetSnapshot::sortOrder))
        .map(snapshot -> toResult(snapshot, deliveries))
        .toList();
  }

  private Map<UUID, OrderReferenceAssetDelivery> resolveDeliveries(
      List<ReferenceAssetSnapshot> snapshots) {
    List<UUID> assetIds =
        snapshots.stream().map(ReferenceAssetSnapshot::assetId).distinct().toList();
    if (assetIds.isEmpty()) {
      return Map.of();
    }

    Map<UUID, Asset> assetsById = assetPersistencePort.findAllById(assetIds).stream()
        .collect(Collectors.toMap(Asset::getId, Function.identity()));
    Map<UUID, AssetVariantDelivery> variantDeliveries =
        assetVariantDeliveryService.resolveReadyDeliveries(assetIds);

    return assetIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            assetId -> resolveOne(
                assetsById.get(assetId),
                variantDeliveries.getOrDefault(assetId, AssetVariantDelivery.empty()))));
  }

  private OrderReferenceAssetDelivery resolveOne(
      Asset asset, AssetVariantDelivery variantDelivery) {
    if (asset == null) {
      return OrderReferenceAssetDelivery.missing();
    }

    String deliveryUrl = variantDelivery.deliveryUrl() == null
        ? assetDeliveryUrlResolver.resolve(asset.getObjectKey())
        : variantDelivery.deliveryUrl();
    return new OrderReferenceAssetDelivery(
        asset.getStatus().name(),
        deliveryUrl,
        variantDelivery.variants().stream()
            .map(variant -> new OrderReferenceAssetResult.Variant(
                variant.type(), variant.deliveryUrl(), variant.width(), variant.height()))
            .toList());
  }

  private OrderReferenceAssetResult toResult(
      ReferenceAssetSnapshot snapshot, Map<UUID, OrderReferenceAssetDelivery> deliveries) {
    OrderReferenceAssetDelivery delivery =
        deliveries.getOrDefault(snapshot.assetId(), OrderReferenceAssetDelivery.missing());
    return new OrderReferenceAssetResult(
        snapshot.assetId(),
        snapshot.source(),
        snapshot.sortOrder(),
        delivery.status(),
        delivery.deliveryUrl(),
        delivery.variants());
  }

  private List<ReferenceAssetSnapshot> readSnapshots(String referenceAssets) {
    if (referenceAssets == null || referenceAssets.isBlank()) {
      return List.of();
    }

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

  private record OrderReferenceAssetDelivery(
      String status, String deliveryUrl, List<OrderReferenceAssetResult.Variant> variants) {

    private OrderReferenceAssetDelivery {
      variants = List.copyOf(variants);
    }

    private static OrderReferenceAssetDelivery missing() {
      return new OrderReferenceAssetDelivery(MISSING_STATUS, null, List.of());
    }
  }
}
