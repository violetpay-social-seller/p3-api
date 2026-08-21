package io.point3.p3api.assetvariant.controller;

import io.point3.p3api.assetvariant.application.query.AssetVariantQueryUseCase;
import io.point3.p3api.common.web.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets/{assetId}/variants")
@RequiredArgsConstructor
public class AssetVariantQueryController {
  private final AssetVariantQueryUseCase assetVariantQueryUseCase;

  @GetMapping
  public ApiResponse<AssetVariantResponse> getVariants(@PathVariable UUID assetId) {
    return ApiResponse.ok(AssetVariantResponse.from(assetVariantQueryUseCase.getVariants(assetId)));
  }
}
