package io.point3.p3api.seller.application.query;

import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import java.util.UUID;

public interface SellerOnboardingCurrentQueryUseCase {

  SellerOnboardingDetailResult getCurrentOnboarding(UUID applicantUserId);
}
