package io.point3.p3api.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.GlobalExceptionHandler;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.businesshours.query.StoreBusinessHoursQueryUseCase;
import io.point3.p3api.store.application.businesshours.update.StoreBusinessHoursUpdateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.query.StoreLocationSearchUseCase;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import io.point3.p3api.store.application.management.StoreManagementStatusQueryUseCase;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.setting.query.StoreSettingQueryUseCase;
import io.point3.p3api.store.application.setting.update.StoreSettingUpdateUseCase;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.infrastructure.web.StoreWebProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class SellerStoreLocationControllerWebTest {

  private final StoreLocationSearchUseCase storeLocationSearchUseCase =
      mock(StoreLocationSearchUseCase.class);

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    SellerStoreController controller = new SellerStoreController(
        mock(StoreCreateUseCase.class),
        mock(StoreQueryUseCase.class),
        mock(StoreUpdateUseCase.class),
        mock(StoreDeleteUseCase.class),
        mock(StoreBusinessHoursQueryUseCase.class),
        mock(StoreBusinessHoursUpdateUseCase.class),
        mock(StoreManagementStatusQueryUseCase.class),
        mock(StoreSettingQueryUseCase.class),
        mock(StoreSettingUpdateUseCase.class),
        storeLocationSearchUseCase,
        new StoreWebProperties("https://p3.example.test"));
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new CurrentStoreIdArgumentResolver())
        .build();
  }

  @Test
  void searchesStoreLocations() throws Exception {
    when(storeLocationSearchUseCase.search(any()))
        .thenReturn(List.of(new StoreLocationResult(
            "강남역센트럴푸르지오시티", "서울특별시 강남구 테헤란로 123", "서울특별시 강남구 역삼동 123-45", "06234")));

    mockMvc
        .perform(get("/seller/store/locations/search").param("query", "테헤란로 123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.items[0].buildingName").value("강남역센트럴푸르지오시티"))
        .andExpect(jsonPath("$.data.items[0].roadAddress").value("서울특별시 강남구 테헤란로 123"))
        .andExpect(jsonPath("$.data.items[0].zipCode").value("06234"));

    ArgumentCaptor<SearchStoreLocationCommand> captor =
        ArgumentCaptor.forClass(SearchStoreLocationCommand.class);
    verify(storeLocationSearchUseCase).search(captor.capture());
    assertEquals("테헤란로 123", captor.getValue().query());
  }

  @Test
  void returnsEmptyItemsWhenSearchHasNoResults() throws Exception {
    when(storeLocationSearchUseCase.search(any())).thenReturn(List.of());

    mockMvc
        .perform(get("/seller/store/locations/search").param("query", "없는주소"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.items").isEmpty());
  }

  @Test
  void rejectsBlankShortAndOverlongQuery() throws Exception {
    mockMvc
        .perform(get("/seller/store/locations/search").param("query", " "))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/seller/store/locations/search").param("query", "역"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/seller/store/locations/search").param("query", "a".repeat(101)))
        .andExpect(status().isBadRequest());
  }

  private static class CurrentStoreIdArgumentResolver implements HandlerMethodArgumentResolver {

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
      return UUID.randomUUID();
    }
  }
}
