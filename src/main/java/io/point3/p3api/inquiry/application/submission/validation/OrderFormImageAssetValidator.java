package io.point3.p3api.inquiry.application.submission.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 이미지 답변 Asset 존재와 제출자 소유권 검증 */
@Component
@RequiredArgsConstructor
public class OrderFormImageAssetValidator {

  private final AssetPersistencePort assetPersistencePort;

  public void validate(
      List<OrderFormOptionGroupResult> optionGroups,
      List<CreateOrderFormSubmissionCommand.FormAnswer> answers,
      UUID ownerId) {
    Map<UUID, OrderFormOptionGroupResult> optionGroupMap = optionGroups.stream()
        .collect(Collectors.toMap(OrderFormOptionGroupResult::id, Function.identity()));

    answers.forEach(
        answer -> validateAnswer(optionGroupMap.get(answer.optionGroupId()), answer, ownerId));
  }

  private void validateAnswer(
      OrderFormOptionGroupResult optionGroup,
      CreateOrderFormSubmissionCommand.FormAnswer answer,
      UUID ownerId) {
    if (optionGroup == null || answer.value() == null || !answer.value().isArray()) {
      return;
    }

    Map<String, OrderFormOptionResult> optionMap = optionGroup.options().stream()
        .collect(Collectors.toMap(OrderFormOptionResult::value, Function.identity()));

    for (JsonNode selected : answer.value()) {
      JsonNode optionValueNode = selected.get("optionValue");
      if (optionValueNode == null || !optionValueNode.isTextual()) {
        continue;
      }

      OrderFormOptionResult option = optionMap.get(optionValueNode.asText());
      if (option == null || option.inputType() != OptionInputType.IMAGE) {
        continue;
      }

      JsonNode assetIds = selected.get("assetIds");
      if (assetIds == null || !assetIds.isArray()) {
        continue;
      }

      assetIds.forEach(assetId -> validateAsset(UUID.fromString(assetId.asText()), ownerId));
    }
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
