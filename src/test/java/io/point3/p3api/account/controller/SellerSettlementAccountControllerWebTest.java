package io.point3.p3api.account.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.account.application.settlement.RegisterSellerSettlementAccountCommand;
import io.point3.p3api.account.application.settlement.SellerSettlementAccountQueryUseCase;
import io.point3.p3api.account.application.settlement.SellerSettlementAccountRegisterUseCase;
import io.point3.p3api.account.application.settlement.SellerSettlementAccountResult;
import io.point3.p3api.account.application.settlement.SettlementBankQueryUseCase;
import io.point3.p3api.account.application.settlement.SettlementBankResult;
import io.point3.p3api.account.domain.type.AccountHolderType;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class SellerSettlementAccountControllerWebTest {

  private final SellerSettlementAccountRegisterUseCase registerUseCase =
      mock(SellerSettlementAccountRegisterUseCase.class);
  private final SellerSettlementAccountQueryUseCase queryUseCase =
      mock(SellerSettlementAccountQueryUseCase.class);
  private final SettlementBankQueryUseCase bankQueryUseCase =
      mock(SettlementBankQueryUseCase.class);

  private MockMvc mockMvc;
  private UUID storeId;

  @BeforeEach
  void setUp() {
    storeId = UUID.randomUUID();
    SellerSettlementAccountController controller =
        new SellerSettlementAccountController(registerUseCase, queryUseCase, bankQueryUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentStoreIdArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("판매자 정산계좌를 등록한다")
  void registersSettlementAccount() throws Exception {
    when(registerUseCase.register(any())).thenReturn(result());

    mockMvc
        .perform(put("/seller/store/settlement-account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "bankCode":"004",
                  "accountNumber":"123-456-789012",
                  "accountHolderName":"홍길동",
                  "holderType":"PERSONAL",
                  "birthDate":"1990-01-02"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.bankCode").value("004"))
        .andExpect(jsonPath("$.data.bankName").value("KB국민은행"))
        .andExpect(jsonPath("$.data.accountNumberMasked").value("********9012"))
        .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

    ArgumentCaptor<RegisterSellerSettlementAccountCommand> captor =
        ArgumentCaptor.forClass(RegisterSellerSettlementAccountCommand.class);
    verify(registerUseCase).register(captor.capture());
    assertEquals(storeId, captor.getValue().storeId());
    assertEquals(LocalDate.of(1990, 1, 2), captor.getValue().birthDate());
  }

  @Test
  @DisplayName("명의자 유형과 맞지 않는 인증정보를 거부한다")
  void rejectsMismatchedHolderInformation() throws Exception {
    mockMvc
        .perform(put("/seller/store/settlement-account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "bankCode":"004",
                  "accountNumber":"123456789012",
                  "accountHolderName":"위하다",
                  "holderType":"BUSINESS",
                  "birthDate":"1990-01-02"
                }
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("등록된 정산계좌를 조회한다")
  void getsSettlementAccount() throws Exception {
    when(queryUseCase.get(storeId)).thenReturn(result());

    mockMvc
        .perform(get("/seller/store/settlement-account"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accountHolderName").value("홍길동"))
        .andExpect(jsonPath("$.data.holderType").value("PERSONAL"));

    verify(queryUseCase).get(storeId);
  }

  @Test
  @DisplayName("선택 가능한 정산은행 목록을 조회한다")
  void getsSettlementBanks() throws Exception {
    when(bankQueryUseCase.getBanks())
        .thenReturn(List.of(
            new SettlementBankResult("004", "KB국민은행"), new SettlementBankResult("088", "신한은행")));

    mockMvc
        .perform(get("/seller/store/settlement-account/banks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].code").value("004"))
        .andExpect(jsonPath("$.data[1].name").value("신한은행"));
  }

  private SellerSettlementAccountResult result() {
    return new SellerSettlementAccountResult(
        "004",
        "KB국민은행",
        "********9012",
        "홍길동",
        AccountHolderType.PERSONAL,
        Instant.parse("2026-09-11T01:00:00Z"));
  }

  private class CurrentStoreIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return parameter.hasParameterAnnotation(CurrentStoreId.class)
          && parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
      return storeId;
    }
  }
}
