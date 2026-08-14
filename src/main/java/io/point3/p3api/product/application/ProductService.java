package io.point3.p3api.product.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ProductErrorCode;
import io.point3.p3api.product.application.create.ProductCreateCommand;
import io.point3.p3api.product.application.create.ProductCreateUseCase;
import io.point3.p3api.product.application.delete.ProductDeleteUseCase;
import io.point3.p3api.product.application.port.ProductPersistencePort;
import io.point3.p3api.product.application.query.ProductQueryUseCase;
import io.point3.p3api.product.application.result.ProductResult;
import io.point3.p3api.product.application.update.ChangeProductStatusCommand;
import io.point3.p3api.product.application.update.ProductUpdateUseCase;
import io.point3.p3api.product.application.update.UpdateProductCommand;
import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService
    implements ProductCreateUseCase,
        ProductUpdateUseCase,
        ProductDeleteUseCase,
        ProductQueryUseCase {

  private final ProductPersistencePort productPersistencePort;

  @Override
  public ProductResult create(ProductCreateCommand command) {
    Product product = Product.create(
        command.storeId(), command.name(), command.description(), command.basePrice());

    return ProductResult.from(productPersistencePort.save(product));
  }

  @Override
  public ProductResult update(UpdateProductCommand command) {
    Product product = findProduct(command.storeId(), command.productId());
    product.updateBasicInfo(command.name(), command.description(), command.basePrice());

    return ProductResult.from(product);
  }

  @Override
  public ProductResult changeStatus(ChangeProductStatusCommand command) {
    Product product = findProduct(command.storeId(), command.productId());

    if (command.status() == ProductStatus.VISIBLE) {
      product.show();
      return ProductResult.from(product);
    }

    if (command.status() == ProductStatus.HIDDEN) {
      product.hide();
      return ProductResult.from(product);
    }

    throw new BaseException(ProductErrorCode.PRODUCT_STATUS_FORBIDDEN);
  }

  @Override
  public void delete(UUID storeId, UUID productId) {
    Product product = findProduct(storeId, productId);
    productPersistencePort.delete(product);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductResult> getSellerProducts(UUID storeId) {
    return productPersistencePort.findAllByStoreId(storeId).stream()
        .map(ProductResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResult getSellerProduct(UUID storeId, UUID productId) {
    return ProductResult.from(findProduct(storeId, productId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductResult> getPublicProducts(UUID storeId) {
    return productPersistencePort.findAllByStoreIdAndStatus(storeId, ProductStatus.VISIBLE).stream()
        .map(ProductResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResult getPublicProduct(UUID storeId, UUID productId) {
    return ProductResult.from(productPersistencePort
        .findByIdAndStoreIdAndStatus(productId, storeId, ProductStatus.VISIBLE)
        .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND)));
  }

  private Product findProduct(UUID storeId, UUID productId) {
    return productPersistencePort
        .findByIdAndStoreId(productId, storeId)
        .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));
  }
}
