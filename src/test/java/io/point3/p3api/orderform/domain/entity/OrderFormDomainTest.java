package io.point3.p3api.orderform.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormDomainTest {

  @Test
  @DisplayName("주문서 템플릿은 활성 상태로 생성되고 이름 변경과 비활성화를 지원한다")
  void changesTemplateNameAndActiveState() {
    OrderFormTemplate template = OrderFormTemplate.create(UUID.randomUUID(), "기본 주문서");

    template.updateName("수정 주문서");
    template.inactive();

    assertEquals("수정 주문서", template.getName());
    assertFalse(template.isActive());
  }

  @Test
  @DisplayName("주문서 카테고리 그룹과 옵션그룹은 음수 정렬 순서를 허용하지 않는다")
  void rejectsNegativeSortOrderForCategoryGroupAndOptionGroup() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormCategoryGroup.create(
            UUID.randomUUID(), OrderFormCategory.SIZE, "사이즈", null, -1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            OrderFormOptionGroup.create(UUID.randomUUID(), "메뉴명", SelectionType.SINGLE, true, -1));
  }

  @Test
  @DisplayName("주문서 옵션은 활성 상태로 생성되고 비활성화할 수 있다")
  void createsAndInactivatesOption() {
    OrderFormOption option = OrderFormOption.create(
        UUID.randomUUID(), "1호", "size-1", OptionInputType.SELECT, 1000L, null, 0);

    assertTrue(option.isActive());
    option.inactive();

    assertEquals("1호", option.getLabel());
    assertEquals("size-1", option.getValue());
    assertEquals(1000, option.getPrice());
    assertFalse(option.isActive());
  }

  @Test
  @DisplayName("주문서 옵션은 음수 가격과 정렬 순서를 허용하지 않는다")
  void rejectsNegativePriceAndSortOrderForOption() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormOption.create(
            UUID.randomUUID(), "1호", "size-1", OptionInputType.SELECT, -1L, null, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormOption.create(
            UUID.randomUUID(), "1호", "size-1", OptionInputType.SELECT, 0L, null, -1));
  }
}
