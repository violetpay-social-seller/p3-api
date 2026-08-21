package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class InquiryPersistenceAdapter implements InquiryPersistencePort {

  private final InquiryJpaRepository inquiryJpaRepository;

  @Override
  public Inquiry save(Inquiry inquiry) {
    return inquiryJpaRepository.save(inquiry);
  }

  @Override
  public Optional<Inquiry> findById(UUID inquiryId) {
    return inquiryJpaRepository.findById(inquiryId);
  }

  @Override
  public Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID buyerUserId) {
    return inquiryJpaRepository.findByStoreIdAndBuyerUserId(storeId, buyerUserId);
  }

  @Override
  public List<Inquiry> findAllByBuyerUserId(UUID buyerUserId) {
    return inquiryJpaRepository.findAllByBuyerUserIdOrderByCreatedAtDesc(buyerUserId);
  }

  @Override
  public List<Inquiry> findAllByStoreId(UUID storeId) {
    return inquiryJpaRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
  }
}
