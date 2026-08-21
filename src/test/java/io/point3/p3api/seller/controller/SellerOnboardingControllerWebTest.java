package io.point3.p3api.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.user.domain.type.UserRole;
import java.time.Instant;
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

class SellerOnboardingControllerWebTest {

  private final SellerOnboardingCreateUseCase sellerOnboardingCreateUseCase = mock(
      SellerOnboardingCreateUseCase.class);

  private MockMvc mockMvc;
  private CurrentUser currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new CurrentUser(UUID.randomUUID(), "판매자", UserRole.SELLER);
    SellerOnboardingController controller = new SellerOnboardingController(
        sellerOnboardingCreateUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentSellerArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("유효한 입점 신청 요청은 PENDING 상태를 반환한다")
  void createsOnboarding() throws Exception {
    UUID onboardingId = UUID.randomUUID();
    when(sellerOnboardingCreateUseCase.create(any())).thenReturn(new SellerOnboardingResult(
        onboardingId,
        UUID.randomUUID(),
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        "https://instagram.com/p3bakery",
        SellerOnboardingStatus.PENDING,
        Instant.parse("2026-08-21T00:00:00Z")));

    mockMvc.perform(post("/seller/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
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
    mockMvc.perform(post("/seller/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
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

  @Test
  @DisplayName("SELLER가 아닌 사용자의 입점 신청은 거절한다")
  void rejectsOnboardingForNonSeller() throws Exception {
    currentUser = new CurrentUser(UUID.randomUUID(), "구매자", UserRole.BUYER);

    mockMvc.perform(post("/seller/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "storeName": "P3 베이커리",
                  "phoneNumber": "010-1234-5678",
                  "address": "서울특별시 중구"
                }
                """))
        .andExpect(status().isUnauthorized())
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
}
