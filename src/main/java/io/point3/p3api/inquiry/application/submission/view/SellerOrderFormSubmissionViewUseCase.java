package io.point3.p3api.inquiry.application.submission.view;

import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
import java.util.UUID;

public interface SellerOrderFormSubmissionViewUseCase {

  OrderFormSubmissionResult markViewed(UUID inquiryId, UUID submissionId, UUID storeId);
}
