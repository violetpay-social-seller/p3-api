package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormOptionGroupJpaRepository
    extends JpaRepository<OrderFormOptionGroup, UUID> {

  List<OrderFormOptionGroup> findAllByCategoryGroupIdInOrderBySortOrderAsc(
      List<UUID> categoryGroupIds);
}
