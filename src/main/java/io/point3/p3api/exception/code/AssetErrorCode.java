package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AssetErrorCode implements ErrorCode {
  ASSET_NOT_FOUND(
      "ASSET_NOT_FOUND_404", "Asset not found", HttpStatus.NOT_FOUND, "/errors/asset/not-found"),
  ASSET_CONTENT_TYPE_NOT_ALLOWED(
      "ASSET_CONTENT_TYPE_NOT_ALLOWED_400",
      "Asset content type is not allowed",
      HttpStatus.BAD_REQUEST,
      "/errors/asset/content-type-not-allowed"),
  ASSET_SIZE_EXCEEDED(
      "ASSET_SIZE_EXCEEDED_400",
      "Asset size exceeded",
      HttpStatus.BAD_REQUEST,
      "/errors/asset/size-exceeded"),
  ASSET_VARIANT_ALREADY_EXISTS(
      "ASSET_VARIANT_ALREADY_EXISTS_409",
      "Asset variant already exists",
      HttpStatus.CONFLICT,
      "/errors/asset/variant-already-exists");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  AssetErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
