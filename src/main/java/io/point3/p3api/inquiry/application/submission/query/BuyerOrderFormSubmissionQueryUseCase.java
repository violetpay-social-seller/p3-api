package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.UUID;

public interface BuyerOrderFormSubmissionQueryUseCase {

  OrderFormSubmission getSubmission(UUID inquiryId, UUID submissionId, UUID buyerUserId);
}
