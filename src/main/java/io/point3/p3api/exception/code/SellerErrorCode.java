package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SellerErrorCode implements ErrorCode {
  SELLER_ONBOARDING_PENDING_ALREADY_EXISTS(
      "SELLER_ONBOARDING_PENDING_ALREADY_EXISTS_409",
      "Seller onboarding is already pending",
      HttpStatus.CONFLICT,
      "/errors/seller/onboarding-pending-already-exists"),
  SELLER_ONBOARDING_NOT_FOUND(
      "SELLER_ONBOARDING_NOT_FOUND_404",
      "Seller onboarding not found",
      HttpStatus.NOT_FOUND,
      "/errors/seller/onboarding-not-found"),
  SELLER_ONBOARDING_REVIEW_NOT_ALLOWED(
      "SELLER_ONBOARDING_REVIEW_NOT_ALLOWED_409",
      "Seller onboarding review is not allowed",
      HttpStatus.CONFLICT,
      "/errors/seller/onboarding-review-not-allowed");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  SellerErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
