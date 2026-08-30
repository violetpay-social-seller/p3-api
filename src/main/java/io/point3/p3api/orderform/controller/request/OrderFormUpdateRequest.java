package io.point3.p3api.orderform.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormUpdateRequest(
    @NotBlank @Size(max = 100) String name,
    @Valid @NotEmpty List<OrderFormCategoryGroupRequest> groups) {

  public OrderFormUpdateRequest {
    groups = groups == null ? null : List.copyOf(groups);
  }

  @Override
  public List<OrderFormCategoryGroupRequest> groups() {
    return groups == null ? null : List.copyOf(groups);
  }
}
