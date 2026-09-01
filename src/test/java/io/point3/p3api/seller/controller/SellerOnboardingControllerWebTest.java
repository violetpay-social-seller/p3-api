package io.point3.p3api.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.auth.JwtCommandExtractor;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.seller.application.query.SellerOnboardingCurrentQueryUseCase;
import io.point3.p3api.seller.application.reapply.SellerOnboardingReapplicationUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.application.submission.SellerOnboardingSubmissionUseCase;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class SellerOnboardingControllerWebTest {

  private final SellerOnboardingSubmissionUseCase sellerOnboardingSubmissionUseCase =
      mock(SellerOnboardingSubmissionUseCase.class);
  private final SellerOnboardingCurrentQueryUseCase sellerOnboardingCurrentQueryUseCase =
      mock(SellerOnboardingCurrentQueryUseCase.class);
  private final SellerOnboardingReapplicationUseCase sellerOnboardingReapplicationUseCase =
      mock(SellerOnboardingReapplicationUseCase.class);
  private final JwtCommandExtractor jwtCommandExtractor = mock(JwtCommandExtractor.class);
  private final Jwt jwt = mock(Jwt.class);

  private MockMvc mockMvc;
  private CurrentUser currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new CurrentUser(UUID.randomUUID(), "판매자", UserRole.SELLER);
    SellerOnboardingController controller = new SellerOnboardingController(
        sellerOnboardingSubmissionUseCase,
        sellerOnboardingCurrentQueryUseCase,
        sellerOnboardingReapplicationUseCase,
        jwtCommandExtractor);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentSellerArgumentResolver(), new JwtArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("판매자는 자신의 최신 입점 신청 상태를 조회할 수 있다")
  void getsCurrentOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    when(sellerOnboardingCurrentQueryUseCase.getCurrentOnboarding(currentUser.userId()))
        .thenReturn(new SellerOnboardingDetailResult(
            onboardingId,
            currentUser.userId(),
            "P3 베이커리",
            "010-1234-5678",
            "서울특별시 중구",
            null,
            SellerOnboardingStatus.REJECTED,
            "사업자 정보가 충분하지 않습니다.",
            Instant.parse("2026-08-22T00:00:00Z"),
            Instant.parse("2026-08-21T00:00:00Z")));

    mockMvc
        .perform(get("/seller/onboardings/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(onboardingId.toString()))
        .andExpect(jsonPath("$.data.status").value("REJECTED"))
        .andExpect(jsonPath("$.data.rejectionReason").value("사업자 정보가 충분하지 않습니다."));
  }

  @Test
  @DisplayName("판매자는 반려된 입점 신청을 재신청할 수 있다")
  void reappliesOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    when(sellerOnboardingReapplicationUseCase.reapply(any()))
        .thenReturn(new SellerOnboardingResult(
            UUID.randomUUID(),
            currentUser.userId(),
            "P3 베이커리",
            "010-1234-5678",
            "서울특별시 중구",
            null,
            SellerOnboardingStatus.PENDING,
            Instant.parse("2026-08-22T00:00:00Z")));

    mockMvc
        .perform(post("/seller/onboardings/{onboardingId}/resubmissions", onboardingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "storeName": "P3 베이커리",
                  "phoneNumber": "010-1234-5678",
                  "address": "서울특별시 중구"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  @DisplayName("유효한 입점 신청 요청은 PENDING 상태를 반환한다")
  void createsOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    when(jwtCommandExtractor.extractRegistration(any(), eq(UserRole.SELLER), eq("010-1234-5678")))
        .thenReturn(CompleteRegistrationCommand.of(
            "cognito-sub",
            "seller@example.com",
            "카카오 닉네임",
            UserRole.SELLER,
            "010-1234-5678",
            SignupProvider.KAKAO));
    when(sellerOnboardingSubmissionUseCase.submit(any()))
        .thenReturn(new SellerOnboardingResult(
            onboardingId,
            UUID.randomUUID(),
            "P3 베이커리",
            "010-1234-5678",
            "서울특별시 중구",
            "https://instagram.com/p3bakery",
            SellerOnboardingStatus.PENDING,
            Instant.parse("2026-08-21T00:00:00Z")));

    mockMvc
        .perform(
            post("/seller/onboardings").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "storeName": "P3 베이커리",
                  "phoneNumber": "010-1234-5678",
                  "address": "서울특별시 중구",
                  "snsLink": "https://instagram.com/p3bakery"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(onboardingId.toString()))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  @DisplayName("필수값 또는 형식이 잘못된 입점 신청 요청은 400을 반환한다")
  void rejectsInvalidRequest() throws Exception {
    mockMvc
        .perform(
            post("/seller/onboardings").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "storeName": "",
                  "phoneNumber": "02-123-4567",
                  "address": "",
                  "snsLink": "instagram.com/p3bakery"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  private class CurrentSellerArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return parameter.hasParameterAnnotation(Authenticated.class)
          && parameter.getParameterType().equals(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
      return currentUser;
    }
  }

  private class JwtArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
          && parameter.getParameterType().equals(Jwt.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
      return jwt;
    }
  }
}
