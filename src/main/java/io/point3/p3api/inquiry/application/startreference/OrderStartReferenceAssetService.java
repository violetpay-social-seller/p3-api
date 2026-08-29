package io.point3.p3api.inquiry.application.startreference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private final OrderStartReferenceAssetPersistencePort orderStartReferenceAssetPersistencePort;
  private final ObjectMapper objectMapper;

  public void replaceIfPresent(
      UUID inquiryId,
      UUID submittedBy,
      List<OrderFormDraftData.ReferenceAsset> startReferenceAssets) {
    if (startReferenceAssets == null || startReferenceAssets.isEmpty()) {
      return;
    }

    orderStartReferenceAssetPersistencePort.deleteAllByInquiryId(inquiryId);
    orderStartReferenceAssetPersistencePort.saveAll(startReferenceAssets.stream()
        .map(asset -> OrderStartReferenceAsset.create(
            inquiryId,
            submittedBy,
            asset.assetId(),
            asset.source(),
            write(new StartReferenceAssetSnapshot(
                asset.assetId(), asset.source().name(), asset.sortOrder())),
            asset.sortOrder()))
        .toList());
  }

  @Transactional(readOnly = true)
  public List<OrderStartReferenceAssetResult> findAllByInquiryId(UUID inquiryId) {
    return orderStartReferenceAssetPersistencePort.findAllByInquiryId(inquiryId).stream()
        .map(OrderStartReferenceAssetResult::from)
        .toList();
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

  private record StartReferenceAssetSnapshot(UUID assetId, String source, int sortOrder) {}
}
