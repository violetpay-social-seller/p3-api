package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
import java.util.List;
import java.util.UUID;

/**
 * 조회/권한 검증용
 */
public interface SellerOrderFormSubmissionQueryUseCase {

  List<OrderFormSubmissionResult> getSubmissions(UUID inquiryId, UUID storeId);

  OrderFormSubmissionResult getSubmission(UUID inquiryId, UUID submissionId, UUID storeId);
}
