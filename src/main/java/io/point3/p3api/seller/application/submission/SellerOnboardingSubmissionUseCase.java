package io.point3.p3api.seller.application.submission;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;

public interface SellerOnboardingSubmissionUseCase {

  SellerOnboardingResult submit(SubmitSellerOnboardingCommand command);
}
