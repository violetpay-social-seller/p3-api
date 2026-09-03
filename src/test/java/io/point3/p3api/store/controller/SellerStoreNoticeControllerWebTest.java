package io.point3.p3api.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.store.application.notice.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.application.notice.query.StoreNoticeQueryUseCase;
import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import io.point3.p3api.store.application.notice.update.StoreNoticeUpdateUseCase;
import io.point3.p3api.store.domain.type.StoreNoticeType;
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

class SellerStoreNoticeControllerWebTest {

  private final StoreNoticeQueryUseCase storeNoticeQueryUseCase =
      mock(StoreNoticeQueryUseCase.class);
  private final StoreNoticeUpdateUseCase storeNoticeUpdateUseCase =
      mock(StoreNoticeUpdateUseCase.class);

  private MockMvc mockMvc;
  private UUID storeId;

  @BeforeEach
  void setUp() {
    storeId = UUID.randomUUID();
    SellerStoreNoticeController controller =
        new SellerStoreNoticeController(storeNoticeQueryUseCase, storeNoticeUpdateUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentStoreIdArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("판매자는 현재 스토어의 공지를 고정 타입 순서로 조회할 수 있다")
  void getsCurrentStoreNotices() throws Exception {
    when(storeNoticeQueryUseCase.getNotices(storeId)).thenReturn(result());

    mockMvc
        .perform(get("/seller/notices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.notices[0].type").value("PICKUP_DELIVERY"))
        .andExpect(jsonPath("$.data.notices[1].content").doesNotExist());

    verify(storeNoticeQueryUseCase).getNotices(storeId);
  }

  @Test
  @DisplayName("판매자는 현재 스토어 기준으로 공지 정의 전체를 저장할 수 있다")
  void updatesCurrentStoreNotices() throws Exception {
    when(storeNoticeUpdateUseCase.update(any())).thenReturn(result());

    mockMvc
        .perform(put("/seller/notices").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "notices": [
                    {"type":"PICKUP_DELIVERY","content":"픽업 안내"},
                    {"type":"DESIGN_PRODUCTION","content":null},
                    {"type":"PAYMENT","content":"결제 안내"},
                    {"type":"CAKE_CARE","content":"보관 안내"},
                    {"type":"BUSINESS_HOURS","content":"영업시간 안내"}
                  ]
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.notices[2].content").value("결제 안내"));

    ArgumentCaptor<UpdateStoreNoticesCommand> captor =
        ArgumentCaptor.forClass(UpdateStoreNoticesCommand.class);
    verify(storeNoticeUpdateUseCase).update(captor.capture());
    assertEquals(storeId, captor.getValue().storeId());
    assertEquals(
        StoreNoticeType.BUSINESS_HOURS, captor.getValue().notices().getLast().type());
  }

  private StoreNoticeResult result() {
    return new StoreNoticeResult(List.of(
        new StoreNoticeResult.Notice(StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
        new StoreNoticeResult.Notice(StoreNoticeType.DESIGN_PRODUCTION, null),
        new StoreNoticeResult.Notice(StoreNoticeType.PAYMENT, "결제 안내"),
        new StoreNoticeResult.Notice(StoreNoticeType.CAKE_CARE, "보관 안내"),
        new StoreNoticeResult.Notice(StoreNoticeType.BUSINESS_HOURS, "영업시간 안내")));
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
