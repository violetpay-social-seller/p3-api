package io.point3.p3api.product.application.port;

import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPersistencePort {

  Product save(Product product);

  List<Product> findAllByStoreId(UUID storeId);

  List<Product> findAllByStoreIdAndStatus(UUID storeId, ProductStatus status);

  Optional<Product> findByIdAndStoreId(UUID productId, UUID storeId);

  Optional<Product> findByIdAndStoreIdAndStatus(UUID productId, UUID storeId, ProductStatus status);

  void delete(Product product);
}
