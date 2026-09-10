package io.point3.p3api.inquiry.application.submission.revision;

import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.command.RequestOrderFormRevisionCommand;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFormRevisionRequestService implements OrderFormRevisionRequestUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;
  private final ChatTimelineItemPublisher chatTimelineItemPublisher;

  @Override
  public ChatTimelineItemResult requestRevision(RequestOrderFormRevisionCommand command) {
    Inquiry inquiry =
        inquiryChatAccessService.getSellerInquiry(command.inquiryId(), command.storeId());
    OrderFormSubmission submission = orderFormSubmissionPersistencePort
        .findById(command.submissionId())
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));
    validateSubmission(inquiry, submission);

    ChatTimelineItem item = chatTimelineItemPublisher.publishOrderFormRevisionRequest(
        inquiry.getId(), command.sellerUserId(), submission.getId());
    return ChatTimelineItemResult.from(item, null);
  }

  private void validateSubmission(Inquiry inquiry, OrderFormSubmission submission) {
    if (!submission.getInquiryId().equals(inquiry.getId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
    }
  }
}
