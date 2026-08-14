package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormFieldJpaRepository extends JpaRepository<OrderFormField, UUID> {

  List<OrderFormField> findAllByTemplateIdOrderBySortOrderAsc(UUID templateId);

  void deleteByTemplateId(UUID templateId);
}
