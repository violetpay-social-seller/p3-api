package io.point3.p3api.seller.application.reapply;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;

public interface SellerOnboardingReapplicationUseCase {

  SellerOnboardingResult reapply(ReapplySellerOnboardingCommand command);
}
