package io.point3.p3api.inquiry.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ProductErrorCode;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.application.submit.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.submit.SubmitPreOrderResult;
import io.point3.p3api.inquiry.application.submit.SubmitPreOrderUseCase;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.product.application.port.ProductPersistencePort;
import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService implements SubmitPreOrderUseCase {

    private final InquiryPersistencePort inquiryPersistencePort;
    private final ProductPersistencePort productPersistencePort;

    @Override
    public SubmitPreOrderResult submit(SubmitPreOrderCommand command) {
        UUID contextProductId = null;

         // 구매자가 주문서에서 제출한 상품의 유효성 검증
        if (command.productId() != null) {
            Product product = productPersistencePort
                    .findByIdAndStoreIdAndStatus(command.productId(), command.storeId(), ProductStatus.VISIBLE)
                    .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));

            contextProductId = product.getId();
        }

        // 채팅방 기존 채팅방 있으면 재사용 | 없으면 채팅방 생성
        Inquiry inquiry = inquiryPersistencePort
                .findByStoreIdAndBuyerUserId(command.storeId(), command.buyerUserId())
                .orElseGet(() -> Inquiry.create(command.storeId(), command.buyerUserId(), null));

        if (contextProductId != null) {
            // 구매자가 명시적으로 상품문의를 했다면 현재 채팅방의 contextProductId 설정
            inquiry.changeContextProduct(contextProductId);
        } else {
            // 구매자가 명시적으로 선택한 상품이 없다면 현재 채팅방의 contextProductId 비우기
            inquiry.clearContextProduct();
        }

        Inquiry saved = inquiryPersistencePort.save(inquiry);
        return SubmitPreOrderResult.from(saved);
    }
}
