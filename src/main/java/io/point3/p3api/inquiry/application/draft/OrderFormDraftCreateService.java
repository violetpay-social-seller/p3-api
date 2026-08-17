package io.point3.p3api.inquiry.application.draft;

import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.port.OrderFormDraftStorePort;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderFormDraftCreateService implements OrderFormDraftCreateUseCase{

    private final OrderFormDraftStorePort orderFormDraftStorePort;

    @Override
    public OrderFormDraftResult create(CreateOrderFormDraftCommand command) {
        return orderFormDraftStorePort.save(command);
    }

}
