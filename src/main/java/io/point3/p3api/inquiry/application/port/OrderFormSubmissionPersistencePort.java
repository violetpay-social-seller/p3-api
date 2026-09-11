package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderFormSubmissionPersistencePort {

  OrderFormSubmission save(OrderFormSubmission orderFormSubmission);

  Optional<OrderFormSubmission> findById(UUID submissionId);

  Optional<OrderFormSubmission> findByIdForUpdate(UUID submissionId);

  List<OrderFormSubmission> findAllByInquiryId(UUID inquiryId);

  List<OrderFormSubmission> findLatestByInquiryIds(List<UUID> inquiryIds);
}
