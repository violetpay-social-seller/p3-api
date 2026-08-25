package io.point3.p3api.inquiry.application.draft.create;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.port.OrderFormDraftStorePort;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import io.point3.p3api.inquiry.application.submission.validation.OrderFormAnswerValidator;
import io.point3.p3api.inquiry.application.submission.validation.OrderFormReferenceAssetValidator;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * draft 저장 전 양식/공지/답변 검증 후 redis(현재 구현체) 저장 요청
 */
@Service
@RequiredArgsConstructor
public class OrderFormDraftService implements OrderFormDraftCreateUseCase {

  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final OrderFormDraftStorePort orderFormDraftStorePort;
  private final OrderFormAnswerValidator orderFormAnswerValidator;
  private final OrderFormReferenceAssetValidator orderFormReferenceAssetValidator;

  @Override
  public OrderFormDraftResult create(CreateOrderFormDraftCommand command) {
    OrderFormResult activeForm = orderFormQueryUseCase.getActiveTemplate(command.storeId());

    if (!activeForm.id().equals(command.orderFormTemplateId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
    }

    if (!command.noticeAgreed()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOTICE_AGREEMENT_REQUIRED);
    }

    orderFormAnswerValidator.validate(activeForm.fields(), toSubmissionAnswers(command));
    orderFormReferenceAssetValidator.validate(
        command.storeId(), toSubmissionReferenceAssets(command));

    return orderFormDraftStorePort.save(OrderFormDraftData.from(command));
  }

  private static List<CreateOrderFormSubmissionCommand.FormAnswer> toSubmissionAnswers(
      CreateOrderFormDraftCommand command) {
    return command.formAnswers().stream()
        .map(answer ->
            new CreateOrderFormSubmissionCommand.FormAnswer(answer.fieldId(), answer.value()))
        .toList();
  }

  private static List<CreateOrderFormSubmissionCommand.ReferenceAsset> toSubmissionReferenceAssets(
      CreateOrderFormDraftCommand command) {
    return command.referenceAssets().stream()
        .map(asset -> new CreateOrderFormSubmissionCommand.ReferenceAsset(
            asset.assetId(), asset.source(), asset.sortOrder()))
        .toList();
  }
}
