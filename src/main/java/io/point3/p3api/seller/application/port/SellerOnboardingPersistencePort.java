package io.point3.p3api.seller.application.port;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import java.util.UUID;

public interface SellerOnboardingPersistencePort {

  SellerOnboarding save(SellerOnboarding sellerOnboarding);

  boolean existsPendingByApplicantUserId(UUID applicantUserId);
}
