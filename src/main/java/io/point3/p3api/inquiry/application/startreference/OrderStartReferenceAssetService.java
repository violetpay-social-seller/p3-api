package io.point3.p3api.inquiry.application.startreference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.port.OrderStartReferenceAssetPersistencePort;
import io.point3.p3api.inquiry.application.result.OrderStartReferenceAssetResult;
import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStartReferenceAssetService {

  private static final int START_REFERENCE_ASSET_SORT_ORDER = 0;

  private final OrderStartReferenceAssetPersistencePort orderStartReferenceAssetPersistencePort;
  private final AssetVariantDeliveryService assetVariantDeliveryService;
  private final ObjectMapper objectMapper;

  public void replaceIfPresent(
      UUID inquiryId,
      UUID submittedBy,
      OrderFormDraftData.ReferenceAsset startReferenceAsset,
      boolean startReferenceAssetProvided) {
    if (!startReferenceAssetProvided) {
      return;
    }

    orderStartReferenceAssetPersistencePort.deleteAllByInquiryId(inquiryId);
    if (startReferenceAsset == null) {
      return;
    }

    orderStartReferenceAssetPersistencePort.saveAll(List.of(OrderStartReferenceAsset.create(
        inquiryId,
        submittedBy,
        startReferenceAsset.assetId(),
        startReferenceAsset.source(),
        write(new StartReferenceAssetSnapshot(
            startReferenceAsset.assetId(),
            startReferenceAsset.source().name(),
            START_REFERENCE_ASSET_SORT_ORDER)),
        START_REFERENCE_ASSET_SORT_ORDER)));
  }

  public void clear(UUID inquiryId) {
    orderStartReferenceAssetPersistencePort.deleteAllByInquiryId(inquiryId);
  }

  @Transactional(readOnly = true)
  public OrderStartReferenceAssetResult findByInquiryId(UUID inquiryId) {
    return orderStartReferenceAssetPersistencePort.findAllByInquiryId(inquiryId).stream()
        .findFirst()
        .map(this::toResult)
        .orElse(null);
  }

  @Transactional(readOnly = true)
  public String createOrderAssetIdSnapshot(UUID inquiryId) {
    List<UUID> assetIds =
        orderStartReferenceAssetPersistencePort.findAllByInquiryId(inquiryId).stream()
            .map(OrderStartReferenceAsset::getAssetId)
            .toList();
    return write(assetIds);
  }

  private String write(Object snapshot) {
    try {
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private OrderStartReferenceAssetResult toResult(OrderStartReferenceAsset asset) {
    AssetVariantDelivery delivery =
        assetVariantDeliveryService.resolveReadyDelivery(asset.getAssetId());
    return OrderStartReferenceAssetResult.from(asset, delivery.deliveryUrl());
  }

  private record StartReferenceAssetSnapshot(UUID assetId, String source, int sortOrder) {}
}
