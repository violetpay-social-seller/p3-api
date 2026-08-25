package io.point3.p3api.dashboard.application.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record SellerRevenueQueryCommand(UUID storeId, LocalDate startDate, LocalDate endDate) {

  public SellerRevenueQueryCommand {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(startDate, "startDate");
    Objects.requireNonNull(endDate, "endDate");

    if (startDate.isAfter(endDate)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  public static SellerRevenueQueryCommand of(UUID storeId, LocalDate startDate, LocalDate endDate) {
    return new SellerRevenueQueryCommand(storeId, startDate, endDate);
  }
}
