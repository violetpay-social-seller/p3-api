package io.point3.p3api.seller.application.create;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;

public interface SellerOnboardingCreateUseCase {

  SellerOnboardingResult create(CreateSellerOnboardingCommand command);
}
