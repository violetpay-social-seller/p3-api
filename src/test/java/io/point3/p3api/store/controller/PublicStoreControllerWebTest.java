package io.point3.p3api.store.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.store.application.notice.query.StoreNoticeQueryUseCase;
import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import io.point3.p3api.store.application.publicquery.PublicStoreQueryUseCase;
import io.point3.p3api.store.application.setting.availability.StoreOrderSettingAvailabilityQueryUseCase;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class PublicStoreControllerWebTest {

  private final PublicStoreQueryUseCase publicStoreQueryUseCase = mock(PublicStoreQueryUseCase.class);
  private final StoreNoticeQueryUseCase storeNoticeQueryUseCase = mock(StoreNoticeQueryUseCase.class);
  private final StoreOrderSettingAvailabilityQueryUseCase availabilityQueryUseCase =
      mock(StoreOrderSettingAvailabilityQueryUseCase.class);

  private MockMvc mockMvc;
  private UUID storeId;

  @BeforeEach
  void setUp() {
    storeId = UUID.randomUUID();
    PublicStoreController controller =
        new PublicStoreController(
            publicStoreQueryUseCase, storeNoticeQueryUseCase, availabilityQueryUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentStoreIdArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("구매자는 공개 스토어 경로로 스토어 공지사항을 조회할 수 있다")
  void getsPublicStoreNotices() throws Exception {
    when(storeNoticeQueryUseCase.getNotices(storeId)).thenReturn(result());

    mockMvc
        .perform(get("/stores/wihada-cake/notices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.notices[0].type").value("PICKUP_DELIVERY"))
        .andExpect(jsonPath("$.data.notices[0].items[0].content").value("픽업 안내"))
        .andExpect(jsonPath("$.data.notices[2].items[0].content").value("결제 안내"));

    verify(storeNoticeQueryUseCase).getNotices(storeId);
  }

  private StoreNoticeResult result() {
    return new StoreNoticeResult(List.of(
        new StoreNoticeResult.Notice(
            StoreNoticeType.PICKUP_DELIVERY,
            List.of(new StoreNoticeResult.Item("픽업 안내", 0))),
        new StoreNoticeResult.Notice(StoreNoticeType.DESIGN_PRODUCTION, List.of()),
        new StoreNoticeResult.Notice(
            StoreNoticeType.PAYMENT, List.of(new StoreNoticeResult.Item("결제 안내", 0))),
        new StoreNoticeResult.Notice(StoreNoticeType.CAKE_CARE, List.of()),
        new StoreNoticeResult.Notice(StoreNoticeType.BUSINESS_HOURS, List.of())));
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
