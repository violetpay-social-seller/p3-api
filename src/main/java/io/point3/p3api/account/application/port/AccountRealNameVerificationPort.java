package io.point3.p3api.account.application.port;

public interface AccountRealNameVerificationPort {

  AccountRealNameVerificationResult verify(AccountRealNameVerificationRequest request);
}
