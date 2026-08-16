package io.point3.p3api.product.infrastructure.persistence;

import io.point3.p3api.product.application.port.ProductPersistencePort;
import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.entity.ProductOption;
import io.point3.p3api.product.domain.entity.ProductOptionGroup;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPersistencePort {

  private final ProductJpaRepository productJpaRepository;
  private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;
  private final ProductOptionJpaRepository productOptionJpaRepository;

  @Override
  public Product save(Product product) {
    return productJpaRepository.save(product);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findAllByStoreId(UUID storeId) {
    return productJpaRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> findAllByStoreIdAndStatus(UUID storeId, ProductStatus status) {
    return productJpaRepository.findAllByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findByIdAndStoreId(UUID productId, UUID storeId) {
    return productJpaRepository.findByIdAndStoreId(productId, storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findByIdAndStoreIdAndStatus(
      UUID productId, UUID storeId, ProductStatus status) {
    return productJpaRepository.findByIdAndStoreIdAndStatus(productId, storeId, status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductOptionGroup> findOptionGroupsByProductId(UUID productId) {
    return productOptionGroupJpaRepository.findAllByProductIdOrderBySortOrderAsc(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductOption> findActiveOptionsByOptionGroupIds(Collection<UUID> optionGroupIds) {
    if (optionGroupIds.isEmpty()) {
      return List.of();
    }

    return productOptionJpaRepository.findAllByOptionGroupIdInAndActiveTrueOrderBySortOrderAsc(
        optionGroupIds);
  }

  @Override
  public void delete(Product product) {
    productJpaRepository.delete(product);
  }
}
