package io.point3.p3api.store.application.setting.availability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.store.application.setting.query.StoreSettingQueryUseCase;
import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StoreOrderSettingAvailabilityQueryServiceTest {

  private static final UUID STORE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-09-10T00:00:00Z"), KOREA_ZONE_ID);

  private final StoreSettingQueryUseCase storeSettingQueryUseCase =
      mock(StoreSettingQueryUseCase.class);
  private final StoreOrderSettingAvailabilityQueryService service =
      new StoreOrderSettingAvailabilityQueryService(
          storeSettingQueryUseCase,
          new StoreOrderSettingAvailabilityCalculator(),
          CLOCK);

  @Test
  @DisplayName("오늘부터 1년 뒤 같은 날짜까지 주문 가능 일정을 조회한다")
  void allowsOneYearInclusiveRange() {
    LocalDate from = LocalDate.of(2026, 9, 10);
    LocalDate to = LocalDate.of(2027, 9, 10);
    when(storeSettingQueryUseCase.getSetting(STORE_ID))
        .thenReturn(StoreSettingResult.empty(STORE_ID));

    assertEquals(366, service.getAvailability(STORE_ID, from, to).dates().size());
  }

  @Test
  @DisplayName("366일을 초과하는 주문 가능 일정 조회를 거절한다")
  void rejectsOverOneYearRange() {
    LocalDate from = LocalDate.of(2026, 9, 10);
    LocalDate to = LocalDate.of(2027, 9, 11);

    BaseException exception =
        assertThrows(BaseException.class, () -> service.getAvailability(STORE_ID, from, to));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
    verifyNoInteractions(storeSettingQueryUseCase);
  }
}
