package io.point3.p3api.product.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.product.application.create.ProductCreateCommand;
import io.point3.p3api.product.application.create.ProductCreateUseCase;
import io.point3.p3api.product.application.delete.ProductDeleteUseCase;
import io.point3.p3api.product.application.query.ProductQueryUseCase;
import io.point3.p3api.product.application.result.ProductResult;
import io.point3.p3api.product.application.update.ChangeProductStatusCommand;
import io.point3.p3api.product.application.update.ProductUpdateUseCase;
import io.point3.p3api.product.application.update.UpdateProductCommand;
import io.point3.p3api.product.controller.request.ProductCreateRequest;
import io.point3.p3api.product.controller.request.ProductStatusRequest;
import io.point3.p3api.product.controller.request.ProductUpdateRequest;
import io.point3.p3api.product.controller.response.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

  private final ProductCreateUseCase productCreateUseCase;
  private final ProductQueryUseCase productQueryUseCase;
  private final ProductUpdateUseCase productUpdateUseCase;
  private final ProductDeleteUseCase productDeleteUseCase;

  @PostMapping
  public ApiResponse<ProductResponse> create(
      @CurrentStoreId UUID storeId, @Valid @RequestBody ProductCreateRequest request) {
    ProductResult result = productCreateUseCase.create(toCommand(storeId, request));
    return ApiResponse.ok(ProductResponse.from(result));
  }

  @GetMapping
  public ApiResponse<List<ProductResponse>> getProducts(@CurrentStoreId UUID storeId) {
    List<ProductResponse> response = productQueryUseCase.getSellerProducts(storeId).stream()
        .map(ProductResponse::from)
        .toList();
    return ApiResponse.ok(response);
  }

  @GetMapping("/{productId}")
  public ApiResponse<ProductResponse> getProduct(
      @CurrentStoreId UUID storeId, @PathVariable UUID productId) {
    ProductResult result = productQueryUseCase.getSellerProduct(storeId, productId);
    return ApiResponse.ok(ProductResponse.from(result));
  }

  @PatchMapping("/{productId}")
  public ApiResponse<ProductResponse> update(
      @CurrentStoreId UUID storeId,
      @PathVariable UUID productId,
      @Valid @RequestBody ProductUpdateRequest request) {
    ProductResult result = productUpdateUseCase.update(toCommand(storeId, productId, request));
    return ApiResponse.ok(ProductResponse.from(result));
  }

  @PatchMapping("/{productId}/status")
  public ApiResponse<ProductResponse> changeStatus(
      @CurrentStoreId UUID storeId,
      @PathVariable UUID productId,
      @Valid @RequestBody ProductStatusRequest request) {
    ProductResult result = productUpdateUseCase.changeStatus(
        new ChangeProductStatusCommand(storeId, productId, request.status()));
    return ApiResponse.ok(ProductResponse.from(result));
  }

  @DeleteMapping("/{productId}")
  public ApiResponse<Void> delete(@CurrentStoreId UUID storeId, @PathVariable UUID productId) {
    productDeleteUseCase.delete(storeId, productId);
    return ApiResponse.ok();
  }

  private ProductCreateCommand toCommand(UUID storeId, ProductCreateRequest request) {
    return new ProductCreateCommand(
        storeId, request.name(), request.description(), request.basePrice());
  }

  private UpdateProductCommand toCommand(
      UUID storeId, UUID productId, ProductUpdateRequest request) {
    return new UpdateProductCommand(
        storeId, productId, request.name(), request.description(), request.basePrice());
  }
}
