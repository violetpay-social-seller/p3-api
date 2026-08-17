package io.point3.p3api.inquiry.application.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 주문서 제출에 포함된 참고 이미지의 저장용 스냅샷을 만든다. */
@Component
@RequiredArgsConstructor
public class OrderFormReferenceSnapshotFactory {

  private final ObjectMapper objectMapper;

  public String create(List<CreateOrderFormSubmissionCommand.ReferenceAsset> referenceAssets) {
    if (referenceAssets == null || referenceAssets.isEmpty()) {
      return null;
    }

    List<ReferenceAssetSnapshot> snapshots = referenceAssets.stream()
        .map(referenceAsset -> new ReferenceAssetSnapshot(
            referenceAsset.assetId(), referenceAsset.source(), referenceAsset.sortOrder()))
        .toList();

    return write(snapshots);
  }

  private String write(Object snapshot) {
    try {
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private record ReferenceAssetSnapshot(UUID assetId, String source, int sortOrder) {}
}
