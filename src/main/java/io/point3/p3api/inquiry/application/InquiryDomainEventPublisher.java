package io.point3.p3api.inquiry.application;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 주문서/확인서/결제요청 과 같은 카드 이벤트 발행
 */
@Component
@RequiredArgsConstructor
public class InquiryDomainEventPublisher {

    private final ChatEventPort chatEventPort;

    public void publishOrderFormSubmission(UUID inquiryId, UUID buyerUserId, UUID submissionId) {
        chatEventPort.save(ChatEvent.orderFormSubmission(inquiryId, buyerUserId, submissionId));
    }
}
