package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
import java.util.UUID;

public interface BuyerOrderFormSubmissionQueryUseCase {

  OrderFormSubmissionResult getSubmission(UUID inquiryId, UUID submissionId, UUID buyerUserId);
}
