package io.point3.p3api.inquiry.application.open;

import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상담 시작 시 기존 문의방을 재사용하거나 새로 생성하고 상품 컨텍스트를 설정
 */
@Service
@Transactional
@RequiredArgsConstructor
public class InquiryOpenService implements OpenInquiryUseCase {

    private final InquiryPersistencePort inquiryPersistencePort;

    @Override
    public Inquiry open(OpenInquiryCommand command) {
        Inquiry inquiry = inquiryPersistencePort
                .findByStoreIdAndBuyerUserId(command.storeId(), command.buyerUserId())
                .orElseGet(() -> Inquiry.create(command.storeId(), command.buyerUserId(), null));

        if (command.contextProductId() != null) {
            inquiry.changeContextProduct(command.contextProductId());
        } else {
            inquiry.clearContextProduct();
        }

        return inquiryPersistencePort.save(inquiry);
    }
}
