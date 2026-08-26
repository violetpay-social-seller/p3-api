package io.point3.p3api.operator.application.query;

import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;

public record OperatorOnboardingQuery(SellerOnboardingStatus status, OperatorPageQuery pageQuery) {}
