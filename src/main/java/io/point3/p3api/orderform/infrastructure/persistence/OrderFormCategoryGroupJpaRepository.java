package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormCategoryGroupJpaRepository
    extends JpaRepository<OrderFormCategoryGroup, UUID> {

  List<OrderFormCategoryGroup> findAllByTemplateIdOrderBySortOrderAsc(UUID templateId);

  void deleteByTemplateId(UUID templateId);
}
