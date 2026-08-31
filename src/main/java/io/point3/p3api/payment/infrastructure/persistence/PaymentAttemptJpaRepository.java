package io.point3.p3api.payment.infrastructure.persistence;

import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentAttemptJpaRepository
    extends JpaRepository<PaymentAttempt, UUID>, JpaSpecificationExecutor<PaymentAttempt> {
  Optional<PaymentAttempt> findByPoint3SessionId(String point3SessionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select paymentAttempt from PaymentAttempt paymentAttempt where paymentAttempt.id = :id")
  Optional<PaymentAttempt> findByIdForUpdate(@Param("id") UUID id);

  List<PaymentAttempt> findAllByConfirmationIdOrderByCreatedAtDesc(UUID confirmationId);
}
