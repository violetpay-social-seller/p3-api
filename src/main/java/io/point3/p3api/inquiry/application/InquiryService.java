package io.point3.p3api.inquiry.application;

import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.application.submit.*;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService implements SubmitPreOrderUseCase {

    private final ProductContextResolver productContextResolver;
    private final OrderFormSubmissionProcessor orderFormSubmissionProcessor;
    private final InquiryPersistencePort inquiryPersistencePort;
    private final InquiryEventPublisher inquiryEventPublisher;

    @Override
    public SubmitPreOrderResult submit(SubmitPreOrderCommand command) {
        UUID contextProductId = productContextResolver.resolve(
                command.storeId(), command.productId());

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

        Inquiry savedInquiry = inquiryPersistencePort.save(inquiry);

        OrderFormSubmission submission = orderFormSubmissionProcessor.submit(command, savedInquiry.getId());

        inquiryEventPublisher.publishOrderFormSubmission(
                savedInquiry.getId(),
                command.buyerUserId(),
                submission.getId());

        return SubmitPreOrderResult.from(savedInquiry);
    }
}
