package io.point3.p3api.store.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.entity.StoreNotice;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class StoreNoticePersistenceAdapterIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StoreNoticeJpaRepository storeNoticeJpaRepository;

  @Autowired
  private StoreNoticePersistencePort storeNoticePersistencePort;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("같은 타입에는 순서별로 여러 공지를 저장할 수 있고 순서는 중복될 수 없다")
  void savesMultipleNoticesByTypeAndRejectsDuplicateSortOrder() {
    StoreResult store = createStore();
    storeNoticeJpaRepository.save(
        StoreNotice.create(store.id(), StoreNoticeType.PAYMENT, "결제 완료 후 주문이 확정됩니다.", 0));
    storeNoticeJpaRepository.save(
        StoreNotice.create(store.id(), StoreNoticeType.PAYMENT, "입금 확인 후 제작을 시작합니다.", 1));
    storeNoticeJpaRepository.flush();
    storeNoticeJpaRepository.save(
        StoreNotice.create(store.id(), StoreNoticeType.PAYMENT, "중복 순서 공지입니다.", 1));

    assertThrows(DataIntegrityViolationException.class, storeNoticeJpaRepository::flush);
  }

  @Test
  @DisplayName("스토어 공지를 전체 교체하면 제거한 타입의 기존 공지가 삭제된다")
  void replacesNoticesByStoreId() {
    StoreResult store = createStore();
    storeNoticePersistencePort.replaceAllByStoreId(
        store.id(),
        List.of(
            StoreNotice.create(
                store.id(), StoreNoticeType.PICKUP_DELIVERY, "픽업 시간 10분 전 도착해 주세요.", 0),
            StoreNotice.create(
                store.id(), StoreNoticeType.PICKUP_DELIVERY, "예약 변경은 이틀 전까지 가능합니다.", 1),
            StoreNotice.create(store.id(), StoreNoticeType.PAYMENT, "결제 완료 후 주문이 확정됩니다.", 0)));

    storeNoticePersistencePort.replaceAllByStoreId(
        store.id(),
        List.of(StoreNotice.create(store.id(), StoreNoticeType.CAKE_CARE, "수령 후 냉장 보관해 주세요.", 0)));

    List<StoreNotice> notices = storeNoticePersistencePort.findAllByStoreId(store.id());

    assertEquals(1, notices.size());
    assertEquals(StoreNoticeType.CAKE_CARE, notices.getFirst().getType());
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("store-notice-seller"),
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
