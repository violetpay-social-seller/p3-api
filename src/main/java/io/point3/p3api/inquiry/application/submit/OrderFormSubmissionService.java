package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 주문서 제출 검증/스냅샷/저장 담당
 */
@Component
@RequiredArgsConstructor
public class OrderFormSubmissionService implements OrderFormSubmissionCreateUseCase {

  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final OrderFormSubmissionPersistencePort submissionPersistencePort;
  private final OrderFormAnswerValidator orderFormAnswerValidator;
  private final OrderFormAnswerSnapshotFactory snapshotFactory;
  private final OrderFormReferenceSnapshotFactory referenceSnapshotFactory;

  @Override
  public OrderFormSubmission create(CreateOrderFormSubmissionCommand command) {
    OrderFormResult activeForm = orderFormQueryUseCase.getActiveTemplate(command.storeId());

    validateOrderFormSubmissionRequirements(command, activeForm);

    orderFormAnswerValidator.validate(activeForm.fields(), command.formAnswers());

    String answersSnapshot = snapshotFactory.create(activeForm.fields(), command.formAnswers());

    String referenceAssets = referenceSnapshotFactory.create(command.referenceAssets());

    OrderFormSubmission submission = OrderFormSubmission.create(
        command.inquiryId(),
        activeForm.id(),
        command.buyerUserId(),
        answersSnapshot,
        referenceAssets);

    return submissionPersistencePort.save(submission);
  }

  private static void validateOrderFormSubmissionRequirements(
      CreateOrderFormSubmissionCommand command, OrderFormResult activeForm) {
    if (!activeForm.id().equals(command.orderFormTemplateId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
    }

    if (!command.noticeAgreement().agreed()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOTICE_AGREEMENT_REQUIRED);
    }
  }
}
