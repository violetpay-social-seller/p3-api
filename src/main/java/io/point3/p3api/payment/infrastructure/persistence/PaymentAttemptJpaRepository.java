package io.point3.p3api.payment.infrastructure.persistence;

import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentAttemptJpaRepository
    extends JpaRepository<PaymentAttempt, UUID>, JpaSpecificationExecutor<PaymentAttempt> {
  Optional<PaymentAttempt> findByPoint3SessionId(String point3SessionId);

  List<PaymentAttempt> findAllByConfirmationIdOrderByCreatedAtDesc(UUID confirmationId);
}
