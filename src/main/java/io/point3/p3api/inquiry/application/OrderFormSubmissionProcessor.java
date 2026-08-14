package io.point3.p3api.inquiry.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.submit.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderFormSubmissionProcessor {

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

        String answersSnapshot =
                snapshotFactory.create(activeForm.fields(), command.formAnswers());

        OrderFormSubmission submission = OrderFormSubmission.create(
                inquiryId,
                activeForm.id(),
                command.buyerUserId(),
                answersSnapshot);

        return submissionPersistencePort.save(submission);
    }
}
