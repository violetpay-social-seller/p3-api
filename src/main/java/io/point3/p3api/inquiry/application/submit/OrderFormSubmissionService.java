package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 주문서 제출 검증/스냅샷/저장 담당
 */
@Component
@RequiredArgsConstructor
public class OrderFormSubmissionService {

  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final OrderFormSubmissionPersistencePort submissionPersistencePort;
  private final OrderFormAnswerValidator orderFormAnswerValidator;
  private final OrderFormAnswerSnapshotFactory snapshotFactory;

  public OrderFormSubmission submit(SubmitPreOrderCommand command, UUID inquiryId) {
    OrderFormResult activeForm = orderFormQueryUseCase.getActiveTemplate(command.storeId());

    if (!activeForm.id().equals(command.orderFormTemplateId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
    }

    orderFormAnswerValidator.validate(activeForm.fields(), command.formAnswers());

    String answersSnapshot = snapshotFactory.create(activeForm.fields(), command.formAnswers());

    OrderFormSubmission submission = OrderFormSubmission.create(
        inquiryId, activeForm.id(), command.buyerUserId(), answersSnapshot);

    return submissionPersistencePort.save(submission);
  }
}
