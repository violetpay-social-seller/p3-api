package io.point3.p3api.product.application.create;

import io.point3.p3api.product.application.result.ProductResult;

public interface ProductCreateUseCase {

  ProductResult create(ProductCreateCommand command);
}
