package io.point3.p3api.orderform.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.controller.response.OrderFormResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{slug}/order-form")
@RequiredArgsConstructor
public class PublicOrderFormController {

  private final OrderFormQueryUseCase orderFormQueryUseCase;

  @GetMapping
  public ApiResponse<OrderFormResponse> getActive(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    OrderFormResult result = orderFormQueryUseCase.getActiveTemplate(storeId);
    return ApiResponse.ok(OrderFormResponse.from(result));
  }
}
