package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderFormSubmissionPersistenceAdapter implements OrderFormSubmissionPersistencePort {

  private final OrderFormSubmissionJpaRepository orderFormSubmissionJpaRepository;

  @Override
  public OrderFormSubmission save(OrderFormSubmission submission) {
    return orderFormSubmissionJpaRepository.save(submission);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrderFormSubmission> findById(UUID submissionId) {
    return orderFormSubmissionJpaRepository.findById(submissionId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderFormSubmission> findAllByInquiryId(UUID inquiryId) {
    return orderFormSubmissionJpaRepository.findAllByInquiryIdOrderBySubmittedAtDesc(inquiryId);
  }
}
