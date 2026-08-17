package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.open.OpenInquiryUseCase;
import io.point3.p3api.inquiry.application.result.SubmitPreOrderResult;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.List;
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
  private final OrderFormSubmissionCreateUseCase orderFormSubmissionCreateUseCase;
  private final ChatTimelineItemPublisher timelineItemPublisher;

  @Override
  public SubmitPreOrderResult submit(SubmitPreOrderCommand command) {
    UUID contextProductId = productContextResolver.resolve(command.storeId(), command.productId());

    Inquiry inquiry = openInquiryUseCase.open(
        new OpenInquiryCommand(command.storeId(), command.buyerUserId(), contextProductId));

    OrderFormSubmission submission = orderFormSubmissionCreateUseCase.create(
        toCreateSubmissionCommand(command, inquiry.getId()));

    timelineItemPublisher.publishOrderFormSubmission(
        inquiry.getId(), command.buyerUserId(), submission.getId());

    return SubmitPreOrderResult.from(inquiry, submission);
  }

  private CreateOrderFormSubmissionCommand toCreateSubmissionCommand(
      SubmitPreOrderCommand command, UUID inquiryId) {
    List<CreateOrderFormSubmissionCommand.FormAnswer> formAnswers = command.formAnswers().stream()
        .map(answer ->
            new CreateOrderFormSubmissionCommand.FormAnswer(answer.fieldId(), answer.value()))
        .toList();

    return new CreateOrderFormSubmissionCommand(
        command.storeId(),
        command.buyerUserId(),
        inquiryId,
        command.orderFormTemplateId(),
        formAnswers,
        null,
        new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
        CreateOrderFormSubmissionCommand.emptyReferenceAssets());
  }
}
