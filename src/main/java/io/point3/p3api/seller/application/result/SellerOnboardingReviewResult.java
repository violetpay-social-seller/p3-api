package io.point3.p3api.seller.application.result;

import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingReviewResult(
    UUID id, SellerOnboardingStatus status, UUID reviewedBy, Instant reviewedAt) {}
