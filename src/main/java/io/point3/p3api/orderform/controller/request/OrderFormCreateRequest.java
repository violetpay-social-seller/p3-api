package io.point3.p3api.orderform.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormCreateRequest(
    @NotBlank @Size(max = 100) String name, @Valid @NotEmpty List<OrderFormFieldRequest> fields) {

  public OrderFormCreateRequest {
    fields = fields == null ? null : List.copyOf(fields);
  }

  @Override
  public List<OrderFormFieldRequest> fields() {
    return fields == null ? null : List.copyOf(fields);
  }
}
