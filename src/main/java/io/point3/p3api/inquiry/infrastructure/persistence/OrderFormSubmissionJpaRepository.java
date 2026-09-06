package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderFormSubmissionJpaRepository extends JpaRepository<OrderFormSubmission, UUID> {

  List<OrderFormSubmission> findAllByInquiryIdOrderBySubmittedAtDesc(UUID inquiryId);

  @Query("""
      select submission
      from OrderFormSubmission submission
      where submission.inquiryId in :inquiryIds
        and submission.submittedAt = (
          select max(latest.submittedAt)
          from OrderFormSubmission latest
          where latest.inquiryId = submission.inquiryId
        )
      """)
  List<OrderFormSubmission> findLatestByInquiryIds(@Param("inquiryIds") List<UUID> inquiryIds);
}
