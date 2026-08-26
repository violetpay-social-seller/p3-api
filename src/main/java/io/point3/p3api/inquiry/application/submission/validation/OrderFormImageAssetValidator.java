package io.point3.p3api.inquiry.application.submission.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 이미지 답변 Asset 존재와 제출자 소유권 검증 */
@Component
@RequiredArgsConstructor
public class OrderFormImageAssetValidator {

  private final AssetPersistencePort assetPersistencePort;

  public void validate(
      java.util.List<OrderFormFieldResult> fields,
      java.util.List<CreateOrderFormSubmissionCommand.FormAnswer> answers,
      UUID ownerId) {
    Map<UUID, FieldType> fieldTypes = fields.stream()
        .collect(Collectors.toMap(OrderFormFieldResult::id, OrderFormFieldResult::fieldType));

    answers.stream()
        .filter(answer -> fieldTypes.get(answer.fieldId()) == FieldType.IMAGE)
        .flatMap(answer -> answer.value().valueStream())
        .map(JsonNode::asText)
        .map(UUID::fromString)
        .forEach(assetId -> validateAsset(assetId, ownerId));
  }

  private void validateAsset(UUID assetId, UUID ownerId) {
    if (ownerId == null) {
      assetPersistencePort
          .findById(assetId)
          .orElseThrow(() -> new BaseException(AssetErrorCode.ASSET_NOT_FOUND));
      return;
    }

    assetPersistencePort
        .findByIdAndUploadedBy(assetId, ownerId)
        .orElseThrow(() -> new BaseException(AssetErrorCode.ASSET_NOT_FOUND));
  }
}
