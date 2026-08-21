package io.point3.p3api.payment.infrastructure.persistence;

import io.point3.p3api.payment.application.port.RefundPersistencePort;
import io.point3.p3api.payment.domain.entity.Refund;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class RefundPersistenceAdapter implements RefundPersistencePort {
  private final RefundJpaRepository refundJpaRepository;

  @Override
  public Refund save(Refund refund) {
    return refundJpaRepository.save(refund);
  }

  @Override
  public Optional<Refund> findById(UUID refundId) {
    return refundJpaRepository.findById(refundId);
  }

  @Override
  public List<Refund> findAllByOrderId(UUID orderId) {
    return refundJpaRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId);
  }
}
