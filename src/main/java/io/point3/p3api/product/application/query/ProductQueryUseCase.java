package io.point3.p3api.product.application.query;

import io.point3.p3api.product.application.result.ProductResult;
import java.util.List;
import java.util.UUID;

public interface ProductQueryUseCase {

  List<ProductResult> getSellerProducts(UUID storeId);

  ProductResult getSellerProduct(UUID storeId, UUID productId);

  List<ProductResult> getPublicProducts(UUID storeId);

  ProductResult getPublicProduct(UUID storeId, UUID productId);
}
