package io.point3.p3api.product.infrastructure.persistence;

import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

  List<Product> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

  List<Product> findAllByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, ProductStatus status);

  Optional<Product> findByIdAndStoreId(UUID productId, UUID storeId);

  Optional<Product> findByIdAndStoreIdAndStatus(UUID productId, UUID storeId, ProductStatus status);
}
