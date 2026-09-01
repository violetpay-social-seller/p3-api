package io.point3.p3api.seller.application.submission;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.user.application.registration.UserRegistrationUseCase;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.domain.type.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingSubmissionService implements SellerOnboardingSubmissionUseCase {

  private final UserRegistrationUseCase userRegistrationUseCase;
  private final SellerOnboardingCreateUseCase sellerOnboardingCreateUseCase;

  @Override
  public SellerOnboardingResult submit(SubmitSellerOnboardingCommand command) {
    UserSyncResult registeredUser =
        userRegistrationUseCase.completeRegistration(command.registrationCommand());
    if (registeredUser.role() != UserRole.SELLER) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Seller registration is required");
    }

    return sellerOnboardingCreateUseCase.create(CreateSellerOnboardingCommand.from(
        registeredUser.userId(),
        command.storeName(),
        command.phoneNumber(),
        command.address(),
        command.snsLink()));
  }
}
