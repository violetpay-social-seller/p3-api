package io.point3.p3api.inquiry.application.submission.validation;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.store.application.setting.availability.StoreOrderSettingAvailabilityQueryUseCase;
import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingDateAvailabilityResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 주문서 픽업 가능일, 시간, 휴무일, 리드타임 검증 */
@Component
@RequiredArgsConstructor
public class OrderFormPickupValidator {

  private final StoreOrderSettingAvailabilityQueryUseCase availabilityQueryUseCase;

  public void validate(UUID storeId, CreateOrderFormSubmissionCommand.PickupRequest pickupRequest) {
    if (pickupRequest == null
        || pickupRequest.pickupDate() == null
        || pickupRequest.pickupTime() == null) {
      throwUnavailable();
    }

    StoreOrderSettingDateAvailabilityResult availability = availabilityQueryUseCase
        .getAvailability(storeId, pickupRequest.pickupDate(), pickupRequest.pickupDate())
        .dates()
        .getFirst();
    if (!availability.available()
        || !availability.pickupSlots().contains(pickupRequest.pickupTime())) {
      throwUnavailable();
    }
  }

  private void throwUnavailable() {
    throw new BaseException(OrderFormErrorCode.ORDER_FORM_PICKUP_UNAVAILABLE);
  }
}
