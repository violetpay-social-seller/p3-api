package io.point3.p3api.orderform.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.orderform.domain.type.FieldType;
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
  @DisplayName("주문서 필드 그룹과 필드는 음수 정렬 순서를 허용하지 않는다")
  void rejectsNegativeSortOrderForGroupAndField() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormFieldGroup.create(UUID.randomUUID(), "기본 정보", null, -1));
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormField.create(UUID.randomUUID(), "메뉴명", FieldType.TEXT, true, null, -1));
  }

  @Test
  @DisplayName("주문서 선택지는 활성 상태로 생성되고 비활성화할 수 있다")
  void createsAndInactivatesFieldOption() {
    OrderFormFieldOption option = OrderFormFieldOption.create(UUID.randomUUID(), "1호", "size-1", 0);

    assertTrue(option.isActive());
    option.inactive();

    assertEquals("1호", option.getLabel());
    assertEquals("size-1", option.getValue());
    assertFalse(option.isActive());
  }

  @Test
  @DisplayName("주문서 선택지는 음수 정렬 순서를 허용하지 않는다")
  void rejectsNegativeSortOrderForFieldOption() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderFormFieldOption.create(UUID.randomUUID(), "1호", "size-1", -1));
  }
}
