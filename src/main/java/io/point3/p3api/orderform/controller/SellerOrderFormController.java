package io.point3.p3api.orderform.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.create.OrderFormCreateUseCase;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.application.update.OrderFormUpdateUseCase;
import io.point3.p3api.orderform.application.update.UpdateOrderFormCommand;
import io.point3.p3api.orderform.controller.request.OrderFormCreateRequest;
import io.point3.p3api.orderform.controller.request.OrderFormFieldRequest;
import io.point3.p3api.orderform.controller.request.OrderFormUpdateRequest;
import io.point3.p3api.orderform.controller.response.OrderFormResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/order-forms")
@RequiredArgsConstructor
public class SellerOrderFormController {

  private final OrderFormCreateUseCase orderFormCreateUseCase;
  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final OrderFormUpdateUseCase orderFormUpdateUseCase;

  @PostMapping
  public ApiResponse<OrderFormResponse> create(
      @CurrentStoreId UUID storeId, @Valid @RequestBody OrderFormCreateRequest request) {
    OrderFormResult result = orderFormCreateUseCase.create(toCommand(storeId, request));
    return ApiResponse.ok(OrderFormResponse.from(result));
  }

  @GetMapping("/active")
  public ApiResponse<OrderFormResponse> getActive(@CurrentStoreId UUID storeId) {
    OrderFormResult result = orderFormQueryUseCase.getActiveTemplate(storeId);
    return ApiResponse.ok(OrderFormResponse.from(result));
  }

  @GetMapping("/{templateId}")
  public ApiResponse<OrderFormResponse> getTemplate(
      @CurrentStoreId UUID storeId, @PathVariable UUID templateId) {
    OrderFormResult result = orderFormQueryUseCase.getSellerTemplate(storeId, templateId);
    return ApiResponse.ok(OrderFormResponse.from(result));
  }

  @PatchMapping("/{templateId}")
  public ApiResponse<OrderFormResponse> update(
      @CurrentStoreId UUID storeId,
      @PathVariable UUID templateId,
      @Valid @RequestBody OrderFormUpdateRequest request) {
    OrderFormResult result =
        orderFormUpdateUseCase.update(toCommand(storeId, templateId, request));
    return ApiResponse.ok(OrderFormResponse.from(result));
  }

  @PatchMapping("/{templateId}/inactive")
  public ApiResponse<OrderFormResponse> inactive(
      @CurrentStoreId UUID storeId, @PathVariable UUID templateId) {
    OrderFormResult result = orderFormUpdateUseCase.inactive(storeId, templateId);
    return ApiResponse.ok(OrderFormResponse.from(result));
  }

  private CreateOrderFormCommand toCommand(UUID storeId, OrderFormCreateRequest request) {
    return new CreateOrderFormCommand(storeId, request.name(), toCreateFields(request.fields()));
  }

  private UpdateOrderFormCommand toCommand(
      UUID storeId, UUID templateId, OrderFormUpdateRequest request) {
    return new UpdateOrderFormCommand(
        storeId, templateId, request.name(), toUpdateFields(request.fields()));
  }

  private List<CreateOrderFormCommand.Field> toCreateFields(
      List<OrderFormFieldRequest> fields) {
    return fields.stream()
        .map(field -> new CreateOrderFormCommand.Field(
            field.label(), field.fieldType(), field.required(), field.settings(), field.sortOrder()))
        .toList();
  }

  private List<UpdateOrderFormCommand.Field> toUpdateFields(
      List<OrderFormFieldRequest> fields) {
    return fields.stream()
        .map(field -> new UpdateOrderFormCommand.Field(
            field.label(), field.fieldType(), field.required(), field.settings(), field.sortOrder()))
        .toList();
  }
}
