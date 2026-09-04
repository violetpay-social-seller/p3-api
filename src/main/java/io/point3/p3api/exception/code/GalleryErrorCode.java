package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GalleryErrorCode implements ErrorCode {
  GALLERY_ITEM_NOT_FOUND(
      "GALLERY_ITEM_NOT_FOUND_404",
      "Gallery item not found",
      HttpStatus.NOT_FOUND,
      "/errors/gallery/item-not-found"),
  GALLERY_ASSET_NOT_FOUND(
      "GALLERY_ASSET_NOT_FOUND_404",
      "Gallery asset not found",
      HttpStatus.NOT_FOUND,
      "/errors/gallery/asset-not-found");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  GalleryErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
