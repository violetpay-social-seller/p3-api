package io.point3.p3api.inquiry.application.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerOrderFormSubmissionQueryService implements SellerOrderFormSubmissionQueryUseCase {

    private final InquiryChatAccessService inquiryChatAccessService;
    private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;

    @Override
    public List<OrderFormSubmission> getSubmissions(UUID inquiryId, UUID storeId) {
        Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
        return orderFormSubmissionPersistencePort.findAllByInquiryId(inquiry.getId());
    }

    @Override
    public OrderFormSubmission getSubmission(UUID inquiryId, UUID submissionId, UUID storeId) {
        Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);

        OrderFormSubmission submission = orderFormSubmissionPersistencePort
                .findById(submissionId)
                .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));

        validate(submission, inquiry);

        return submission;
    }

    private static void validate(OrderFormSubmission submission, Inquiry inquiry) {
        if (!submission.getInquiryId().equals(inquiry.getId())) {
            throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
        }
    }
}
