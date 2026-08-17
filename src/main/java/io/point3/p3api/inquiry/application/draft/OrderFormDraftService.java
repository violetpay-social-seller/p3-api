package io.point3.p3api.inquiry.application.draft;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.port.OrderFormDraftStorePort;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderFormDraftService implements OrderFormDraftCreateUseCase ,OrderFormDraftQueryUseCase{

    private final OrderFormDraftStorePort orderFormDraftStorePort;

    @Override
    public OrderFormDraftResult create(CreateOrderFormDraftCommand command) {
        return orderFormDraftStorePort.save(command);
    }

    @Override
    public OrderFormDraftData get(String draftKey) {
        return orderFormDraftStorePort.findByDraftKey(draftKey)
                .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND)); // TODO : 정확한 에러로 변경 필요
    }
}
