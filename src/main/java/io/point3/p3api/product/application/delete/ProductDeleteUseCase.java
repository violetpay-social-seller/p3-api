package io.point3.p3api.product.application.delete;

import java.util.UUID;

public interface ProductDeleteUseCase {

  void delete(UUID storeId, UUID productId);
}
