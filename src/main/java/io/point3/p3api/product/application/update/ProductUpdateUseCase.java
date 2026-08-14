package io.point3.p3api.product.application.update;

import io.point3.p3api.product.application.result.ProductResult;

public interface ProductUpdateUseCase {

  ProductResult update(UpdateProductCommand command);

  ProductResult changeStatus(ChangeProductStatusCommand command);
}
