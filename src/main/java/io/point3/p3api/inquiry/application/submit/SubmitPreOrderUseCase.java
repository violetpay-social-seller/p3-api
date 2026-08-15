package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.result.SubmitPreOrderResult;

public interface SubmitPreOrderUseCase {

  SubmitPreOrderResult submit(SubmitPreOrderCommand command);
}
