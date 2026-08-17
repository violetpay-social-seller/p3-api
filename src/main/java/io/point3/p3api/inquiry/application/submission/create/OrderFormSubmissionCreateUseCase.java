package io.point3.p3api.inquiry.application.submission.create;

import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;

public interface OrderFormSubmissionCreateUseCase {

  OrderFormSubmission create(CreateOrderFormSubmissionCommand command);
}
