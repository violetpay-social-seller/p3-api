package io.point3.p3api.inquiry.application.submission.query;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.List;
import java.util.UUID;

/**
 * 조회/권한 검증용
 */
public interface SellerOrderFormSubmissionQueryUseCase {

  List<OrderFormSubmission> getSubmissions(UUID inquiryId, UUID storeId);

  OrderFormSubmission getSubmission(UUID inquiryId, UUID submissionId, UUID storeId);
}
