package io.point3.p3api.assetvariant.controller;

import io.point3.p3api.assetvariant.application.register.AssetVariantRegisterUseCase;
import io.point3.p3api.assetvariant.application.register.RegisterAssetVariantsCommand;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import io.point3.p3api.common.web.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/assets/{assetId}/variants")
@RequiredArgsConstructor
public class AssetVariantController {

  private final AssetVariantRegisterUseCase assetVariantRegisterUseCase;

  @PostMapping
  public ApiResponse<AssetVariantResponse> registerVariants(
      @PathVariable UUID assetId, @RequestBody AssetVariantRequest request) {
    RegisteredAssetVariants result =
        assetVariantRegisterUseCase.register(RegisterAssetVariantsCommand.from(assetId, request));
    return ApiResponse.ok(AssetVariantResponse.from(result));
  }
}
