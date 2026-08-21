package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormFieldGroupJpaRepository extends JpaRepository<OrderFormFieldGroup, UUID> {

  List<OrderFormFieldGroup> findAllByTemplateIdOrderBySortOrderAsc(UUID templateId);

  void deleteByTemplateId(UUID templateId);
}
