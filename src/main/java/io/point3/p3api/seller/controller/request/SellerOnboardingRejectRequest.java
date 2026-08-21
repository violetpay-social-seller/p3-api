package io.point3.p3api.seller.controller.request;

import jakarta.validation.constraints.NotBlank;

public record SellerOnboardingRejectRequest(@NotBlank String rejectionReason) {}
