package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.draft.OrderFormDraftData;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;

import java.util.Optional;

public interface OrderFormDraftStorePort {

    OrderFormDraftResult save(CreateOrderFormDraftCommand command);

    Optional<OrderFormDraftData> findByDraftKey(String draftKey);

    void delete(String draftKey);
}
