package io.point3.p3api.store.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StoreLocationTest {

  @Test
  @DisplayName("스토어 위치를 한 번 초기화하고 전체 주소로 조합한다")
  void initializesLocationOnce() {
    Store store = Store.create(UUID.randomUUID(), "테스트 스토어", "test-store");

    store.initializeLocation(" 서울특별시  서대문구 연희로 12길 ", " 1층  101호 ");

    assertEquals("서울특별시 서대문구 연희로 12길", store.getAddress());
    assertEquals("1층 101호", store.getDetailAddress());
    assertEquals("서울특별시 서대문구 연희로 12길 1층 101호", store.getFullAddress());
    assertThrows(IllegalStateException.class, () -> store.initializeLocation("서울특별시 마포구", "2층"));
  }

  @Test
  @DisplayName("상세주소 없이 스토어 위치를 초기화할 수 있다")
  void initializesLocationWithoutDetailAddress() {
    Store store = Store.create(UUID.randomUUID(), "테스트 스토어", "test-store");

    store.initializeLocation("서울특별시 서대문구 연희로 12길", "  ");

    assertNull(store.getDetailAddress());
    assertEquals("서울특별시 서대문구 연희로 12길", store.getFullAddress());
  }
}
