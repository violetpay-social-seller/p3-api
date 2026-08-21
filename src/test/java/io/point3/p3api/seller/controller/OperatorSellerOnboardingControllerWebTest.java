package io.point3.p3api.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.seller.application.query.SellerOnboardingPendingQueryUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;
import io.point3.p3api.seller.application.review.SellerOnboardingReviewUseCase;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.user.domain.type.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class OperatorSellerOnboardingControllerWebTest {

  private final SellerOnboardingPendingQueryUseCase sellerOnboardingPendingQueryUseCase = mock(
      SellerOnboardingPendingQueryUseCase.class);
  private final SellerOnboardingReviewUseCase sellerOnboardingReviewUseCase = mock(
      SellerOnboardingReviewUseCase.class);

  private MockMvc mockMvc;
  private CurrentUser currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new CurrentUser(UUID.randomUUID(), "운영자", UserRole.OPERATOR);
    OperatorSellerOnboardingController controller = new OperatorSellerOnboardingController(
        sellerOnboardingPendingQueryUseCase, sellerOnboardingReviewUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("운영자는 대기 중인 판매자 입점 신청 목록을 조회할 수 있다")
  void getsPendingOnboardings() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    UUID applicantUserId = UUID.randomUUID();
    when(sellerOnboardingPendingQueryUseCase.getPendingOnboardings()).thenReturn(List.of(
        new SellerOnboardingResult(
            onboardingId,
            applicantUserId,
            "P3 베이커리",
            "010-1234-5678",
            "서울특별시 중구",
            "https://instagram.com/p3bakery",
            SellerOnboardingStatus.PENDING,
            Instant.parse("2026-08-21T00:00:00Z"))));

    mockMvc.perform(get("/operator/seller-onboardings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(onboardingId.toString()))
        .andExpect(jsonPath("$.data[0].applicantUserId").value(applicantUserId.toString()))
        .andExpect(jsonPath("$.data[0].storeName").value("P3 베이커리"));
  }

  @Test
  @DisplayName("운영자는 대기 중인 판매자 입점 신청을 승인할 수 있다")
  void approvesOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    Instant reviewedAt = Instant.parse("2026-08-21T00:00:00Z");
    when(sellerOnboardingReviewUseCase.approve(any())).thenReturn(
        new SellerOnboardingReviewResult(
            onboardingId, SellerOnboardingStatus.APPROVED, currentUser.userId(), reviewedAt));

    mockMvc.perform(patch("/operator/seller-onboardings/{onboardingId}/approve", onboardingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("APPROVED"))
        .andExpect(jsonPath("$.data.reviewedBy").value(currentUser.userId().toString()));
  }

  @Test
  @DisplayName("운영자는 반려 사유와 함께 대기 중인 판매자 입점 신청을 반려할 수 있다")
  void rejectsOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    Instant reviewedAt = Instant.parse("2026-08-21T00:00:00Z");
    when(sellerOnboardingReviewUseCase.reject(any())).thenReturn(
        new SellerOnboardingReviewResult(
            onboardingId, SellerOnboardingStatus.REJECTED, currentUser.userId(), reviewedAt));

    mockMvc.perform(patch("/operator/seller-onboardings/{onboardingId}/reject", onboardingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "rejectionReason": "사업자 정보가 충분하지 않습니다." }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("REJECTED"));
  }

  @Test
  @DisplayName("반려 사유가 없으면 반려 요청은 400을 반환한다")
  void rejectsOnboardingWithoutRejectionReason() throws Exception {
    UUID onboardingId = UUID.randomUUID();

    mockMvc.perform(patch("/operator/seller-onboardings/{onboardingId}/reject", onboardingId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "rejectionReason": "" }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("운영자가 아닌 사용자는 입점 신청을 심사할 수 없다")
  void rejectsReviewForNonOperator() throws Exception {
    currentUser = new CurrentUser(UUID.randomUUID(), "판매자", UserRole.SELLER);

    mockMvc.perform(patch("/operator/seller-onboardings/{onboardingId}/approve", UUID.randomUUID()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));
  }

  private class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

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
}
