package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.open.OpenInquiryUseCase;
import io.point3.p3api.inquiry.application.result.SubmitPreOrderResult;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PreOrderSubmissionFlowService implements SubmitPreOrderUseCase {

  private final ProductContextResolver productContextResolver;
  private final OpenInquiryUseCase openInquiryUseCase;
  private final OrderFormSubmissionService orderFormSubmissionService;
  private final ChatTimelineItemPublisher timelineItemPublisher;

  @Override
  public SubmitPreOrderResult submit(SubmitPreOrderCommand command) {
    UUID contextProductId = productContextResolver.resolve(command.storeId(), command.productId());

    Inquiry inquiry = openInquiryUseCase.open(
        new OpenInquiryCommand(command.storeId(), command.buyerUserId(), contextProductId));

    OrderFormSubmission submission = orderFormSubmissionService.submit(command, inquiry.getId());

    timelineItemPublisher.publishOrderFormSubmission(
        inquiry.getId(), command.buyerUserId(), submission.getId());

    return SubmitPreOrderResult.from(inquiry, submission);
  }
}
