package io.point3.p3api.order.application.query.order;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record SellerOrderListQuery(
    UUID storeId,
    Set<OrderStatus> statuses,
    LocalDate startDate,
    LocalDate endDate,
    OrderListDateBasis dateBasis) {

  public SellerOrderListQuery {
    if (storeId == null) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "storeId must not be null");
    }
    statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
    dateBasis = dateBasis == null ? OrderListDateBasis.CREATED_AT : dateBasis;
  }

  public static SellerOrderListQuery of(
      UUID storeId,
      Collection<String> statusTexts,
      LocalDate startDate,
      LocalDate endDate,
      String dateBasisText) {
    return new SellerOrderListQuery(
        storeId, parseStatuses(statusTexts), startDate, endDate, parseDateBasis(dateBasisText));
  }

  private static Set<OrderStatus> parseStatuses(Collection<String> statusTexts) {
    if (statusTexts == null || statusTexts.isEmpty()) {
      return Set.of();
    }

    try {
      return statusTexts.stream()
          .filter(Objects::nonNull)
          .flatMap(text -> Arrays.stream(text.split("[,|]")))
          .map(String::trim)
          .filter(text -> !text.isBlank())
          .map(text -> text.toUpperCase(Locale.ROOT))
          .map(OrderStatus::valueOf)
          .collect(Collectors.toUnmodifiableSet());
    } catch (IllegalArgumentException exception) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid order status");
    }
  }

  private static OrderListDateBasis parseDateBasis(String dateBasisText) {
    if (dateBasisText == null || dateBasisText.isBlank()) {
      return OrderListDateBasis.CREATED_AT;
    }

    try {
      return OrderListDateBasis.valueOf(dateBasisText.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid order date basis");
    }
  }
}
