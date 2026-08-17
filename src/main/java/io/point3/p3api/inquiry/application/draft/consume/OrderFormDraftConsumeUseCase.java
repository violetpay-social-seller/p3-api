package io.point3.p3api.inquiry.application.draft.consume;

import io.point3.p3api.inquiry.application.command.ConsumeOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.result.OrderFormDraftConsumeResult;

public interface OrderFormDraftConsumeUseCase {

  OrderFormDraftConsumeResult consume(ConsumeOrderFormDraftCommand command);
}
