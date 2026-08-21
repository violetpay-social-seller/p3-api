package io.point3.p3api.seller.application.query;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import java.util.List;

public interface SellerOnboardingPendingQueryUseCase {

  List<SellerOnboardingResult> getPendingOnboardings();
}
