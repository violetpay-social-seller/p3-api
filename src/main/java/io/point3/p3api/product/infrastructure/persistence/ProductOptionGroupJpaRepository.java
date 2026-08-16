package io.point3.p3api.product.infrastructure.persistence;

import io.point3.p3api.product.domain.entity.ProductOptionGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionGroupJpaRepository extends JpaRepository<ProductOptionGroup, UUID> {

  List<ProductOptionGroup> findAllByProductIdOrderBySortOrderAsc(UUID productId);
}
