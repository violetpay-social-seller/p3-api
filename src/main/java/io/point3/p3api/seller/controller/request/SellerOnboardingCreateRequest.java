package io.point3.p3api.seller.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SellerOnboardingCreateRequest(
    @NotBlank @Size(max = 100) String storeName,

    @NotBlank
    @Pattern(
        regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
        message = "phoneNumber must be a valid Korean mobile phone number")
    String phoneNumber,

    @NotBlank @Size(max = 255) String address,

    @Size(max = 500)
    @Pattern(regexp = "^https?://[^\\s]+$", message = "snsLink must be a valid HTTP or HTTPS URL")
    String snsLink) {}
