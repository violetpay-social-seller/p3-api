package io.point3.p3api.inquiry.application.draft;

import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;

public interface OrderFormDraftCreateUseCase {

    OrderFormDraftResult create(CreateOrderFormDraftCommand command);
}
