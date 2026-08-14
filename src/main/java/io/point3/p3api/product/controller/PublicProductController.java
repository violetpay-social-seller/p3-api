package io.point3.p3api.product.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.product.application.query.ProductQueryUseCase;
import io.point3.p3api.product.application.result.ProductResult;
import io.point3.p3api.product.controller.response.ProductResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{slug}/products")
@RequiredArgsConstructor
public class PublicProductController {

  private final ProductQueryUseCase productQueryUseCase;

  @GetMapping
  public ApiResponse<List<ProductResponse>> getProducts(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    List<ProductResponse> response = productQueryUseCase.getPublicProducts(storeId).stream()
        .map(ProductResponse::from)
        .toList();
    return ApiResponse.ok(response);
  }

  @GetMapping("/{productId}")
  public ApiResponse<ProductResponse> getProduct(
      @PathVariable String slug, @PathVariable UUID productId, @CurrentStoreId UUID storeId) {
    ProductResult result = productQueryUseCase.getPublicProduct(storeId, productId);
    return ApiResponse.ok(ProductResponse.from(result));
  }
}
