package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.domain.entity.OrderFormFieldOption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFormFieldOptionJpaRepository
    extends JpaRepository<OrderFormFieldOption, UUID> {}
