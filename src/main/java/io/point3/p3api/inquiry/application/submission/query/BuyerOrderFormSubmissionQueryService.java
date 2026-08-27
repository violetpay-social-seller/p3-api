package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerOrderFormSubmissionQueryService implements BuyerOrderFormSubmissionQueryUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;

  @Override
  public OrderFormSubmission getSubmission(UUID inquiryId, UUID submissionId, UUID buyerUserId) {
    Inquiry inquiry = inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    OrderFormSubmission submission = orderFormSubmissionPersistencePort
        .findById(submissionId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));

    validate(submission, inquiry, buyerUserId);
    return submission;
  }

  private static void validate(OrderFormSubmission submission, Inquiry inquiry, UUID buyerUserId) {
    if (!submission.getInquiryId().equals(inquiry.getId())
        || !submission.getSubmittedBy().equals(buyerUserId)) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
    }
  }
}
