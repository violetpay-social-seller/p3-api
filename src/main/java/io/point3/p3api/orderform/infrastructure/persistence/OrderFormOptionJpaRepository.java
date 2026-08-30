package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormOptionJpaRepository extends JpaRepository<OrderFormOption, UUID> {

  List<OrderFormOption> findAllByOptionGroupIdInOrderBySortOrderAsc(List<UUID> optionGroupIds);
}
