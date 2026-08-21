package io.point3.p3api.asset.controller;

import io.point3.p3api.asset.application.delete.AssetDeleteUseCase;
import io.point3.p3api.asset.application.query.AssetQueryUseCase;
import io.point3.p3api.asset.application.register.AssetRegisterUseCase;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.asset.application.result.RegistryAsset;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

  private final AssetRegisterUseCase assetRegisterUseCase;
  private final AssetQueryUseCase assetQueryUseCase;
  private final AssetDeleteUseCase assetDeleteUseCase;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<AssetResponse> registerAsset(
      @RequestPart("file") MultipartFile file, @Authenticated CurrentUser currentUser)
      throws IOException {
    RegistryAsset asset = assetRegisterUseCase.register(toCommand(currentUser, file));
    return ApiResponse.ok(AssetResponse.from(asset));
  }

  @GetMapping
  public ApiResponse<List<AssetDetailResponse>> getMyAssets(
      @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(assetQueryUseCase.getMyAssets(currentUser.userId()).stream()
        .map(AssetDetailResponse::from)
        .toList());
  }

  @GetMapping("/{assetId}")
  public ApiResponse<AssetDetailResponse> getAsset(
      @PathVariable UUID assetId, @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(
        AssetDetailResponse.from(assetQueryUseCase.getAsset(assetId, currentUser.userId())));
  }

  @DeleteMapping("/{assetId}")
  public ApiResponse<Void> deleteAsset(
      @PathVariable UUID assetId, @Authenticated CurrentUser currentUser) {
    assetDeleteUseCase.delete(assetId, currentUser.userId());
    return ApiResponse.ok();
  }

  private RegisterAssetCommand toCommand(CurrentUser currentUser, MultipartFile file)
      throws IOException {
    return new RegisterAssetCommand(
        currentUser.userId(),
        file.getInputStream(),
        file.getOriginalFilename(),
        file.getContentType(),
        file.getSize());
  }
}
