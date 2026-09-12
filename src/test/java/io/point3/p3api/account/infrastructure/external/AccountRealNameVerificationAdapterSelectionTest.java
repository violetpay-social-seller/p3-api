package io.point3.p3api.account.infrastructure.external;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.account.application.port.AccountRealNameVerificationPort;
import io.point3.p3api.account.infrastructure.external.kftc.KftcAccessTokenProvider;
import io.point3.p3api.account.infrastructure.external.kftc.KftcAccountRealNameVerificationAdapter;
import io.point3.p3api.account.infrastructure.external.kftc.KftcBankTransactionIdGenerator;
import io.point3.p3api.account.infrastructure.external.kftc.KftcProperties;
import io.point3.p3api.account.infrastructure.external.temporary.TemporaryAccountRealNameVerificationAdapter;
import java.net.http.HttpClient;
import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class AccountRealNameVerificationAdapterSelectionTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(AdapterConfiguration.class)
      .withBean(KftcProperties.class, () -> mock(KftcProperties.class))
      .withBean(KftcAccessTokenProvider.class, () -> mock(KftcAccessTokenProvider.class))
      .withBean(
          KftcBankTransactionIdGenerator.class,
          () -> mock(KftcBankTransactionIdGenerator.class))
      .withBean(ObjectMapper.class, ObjectMapper::new)
      .withBean("kftcHttpClient", HttpClient.class, () -> mock(HttpClient.class))
      .withBean(Clock.class, Clock::systemUTC);

  @Test
  @DisplayName("설정이 없으면 실제 금융결제원 Adapter를 선택한다")
  void selectsKftcAdapterByDefault() {
    contextRunner.run(context -> assertInstanceOf(
        KftcAccountRealNameVerificationAdapter.class,
        context.getBean(AccountRealNameVerificationPort.class)));
  }

  @Test
  @DisplayName("금융결제원 검증을 비활성화하면 임시 Adapter를 선택한다")
  void selectsTemporaryAdapterWhenVerificationIsDisabled() {
    contextRunner
        .withPropertyValues("p3.kftc.verification-enabled=false")
        .run(context -> assertInstanceOf(
            TemporaryAccountRealNameVerificationAdapter.class,
            context.getBean(AccountRealNameVerificationPort.class)));
  }

  @Configuration(proxyBeanMethods = false)
  @Import({
    KftcAccountRealNameVerificationAdapter.class,
    TemporaryAccountRealNameVerificationAdapter.class
  })
  static class AdapterConfiguration {}
}
