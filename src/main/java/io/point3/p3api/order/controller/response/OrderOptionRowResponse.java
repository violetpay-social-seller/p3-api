package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.result.OrderOptionRow;

public record OrderOptionRowResponse(String label, String value, Long amount) {

  public static OrderOptionRowResponse from(OrderOptionRow row) {
    return new OrderOptionRowResponse(row.label(), row.value(), row.amount());
  }
}
