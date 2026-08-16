package io.point3.p3api.product.infrastructure.persistence;

import io.point3.p3api.product.domain.entity.ProductOption;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, UUID> {

  List<ProductOption> findAllByOptionGroupIdInAndActiveTrueOrderBySortOrderAsc(
      Collection<UUID> optionGroupIds);
}
