package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormTemplateJpaRepository extends JpaRepository<OrderFormTemplate, UUID> {

  Optional<OrderFormTemplate> findByIdAndStoreId(UUID templateId, UUID storeId);

  Optional<OrderFormTemplate> findByStoreIdAndActiveTrue(UUID storeId);

  boolean existsByStoreIdAndActiveTrue(UUID storeId);
}
