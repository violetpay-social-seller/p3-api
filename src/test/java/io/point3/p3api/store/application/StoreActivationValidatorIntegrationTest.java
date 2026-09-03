package io.point3.p3api.store.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import io.point3.p3api.orderform.infrastructure.persistence.OrderFormTemplateJpaRepository;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreNotice;
import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreOperationSettingJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreWeeklyPickupSettingJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoreActivationValidatorIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StoreActivationValidator storeActivationValidator;

  @Autowired
  private StoreService storeService;

  @Autowired
  private StoreNoticePersistencePort storeNoticePersistencePort;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private StoreOperationSettingJpaRepository storeOperationSettingJpaRepository;

  @Autowired
  private StoreWeeklyPickupSettingJpaRepository storeWeeklyPickupSettingJpaRepository;

  @Autowired
  private OrderFormTemplateJpaRepository orderFormTemplateJpaRepository;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("5개 공지 중 하나라도 미작성되면 활성화를 거절하고 모두 작성되면 통과시킨다")
  void requiresCompleteNoticesForActivation() {
    StoreResult store = createStore();
    prepareOtherActivationConditions(store.id());
    Store persisted = storeJpaRepository.findById(store.id()).orElseThrow();

    BaseException exception =
        assertThrows(BaseException.class, () -> storeActivationValidator.validate(persisted));

    storeNoticePersistencePort.replaceAllByStoreId(store.id(), completeNotices(store.id()));

    assertEquals(StoreErrorCode.ORDER_NOTICE_REQUIRED, exception.getErrorCode());
    assertDoesNotThrow(() -> storeActivationValidator.validate(persisted));
  }

  private void prepareOtherActivationConditions(UUID storeId) {
    orderFormTemplateJpaRepository.saveAndFlush(OrderFormTemplate.create(storeId, "기본 주문서"));
    storeOperationSettingJpaRepository.saveAndFlush(
        StoreOperationSetting.create(storeId, 60, "레거시 공지", 0));
    storeWeeklyPickupSettingJpaRepository.saveAndFlush(StoreWeeklyPickupSetting.create(
        storeId, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(18, 0), 10, true));
    Store store = storeJpaRepository.findById(storeId).orElseThrow();
    store.markSettlementAccountInputCompleted(Instant.now());
    storeJpaRepository.saveAndFlush(store);
  }

  private List<StoreNotice> completeNotices(UUID storeId) {
    return List.of(
        StoreNotice.create(storeId, StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
        StoreNotice.create(storeId, StoreNoticeType.DESIGN_PRODUCTION, "디자인 안내"),
        StoreNotice.create(storeId, StoreNoticeType.PAYMENT, "결제 안내"),
        StoreNotice.create(storeId, StoreNoticeType.CAKE_CARE, "보관 안내"),
        StoreNotice.create(storeId, StoreNoticeType.BUSINESS_HOURS, "영업시간 안내"));
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("store-activation-seller"),
        "판매자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
    return storeService.create(new CreateStoreCommand(
        seller.getId(),
        "P3 베이커리",
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        null,
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구"));
  }
}
