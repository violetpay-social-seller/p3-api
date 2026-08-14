package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderFormSubmissionJpaRepository extends JpaRepository<OrderFormSubmission, UUID> {


    List<OrderFormSubmission> findAllByInquiryIdOrderBySubmittedAtDesc(UUID inquiryId);
}
