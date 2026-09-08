package io.point3.p3api.order.application.price;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderConfirmationPriceCalculatorTest {

  private static final UUID FIXED_GROUP_ID =
      UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MANUAL_GROUP_ID =
      UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID SECOND_MANUAL_GROUP_ID =
      UUID.fromString("33333333-3333-3333-3333-333333333333");

  private final OrderConfirmationPriceCalculator calculator =
      new OrderConfirmationPriceCalculator(new ObjectMapper());

  @Test
  @DisplayName("미리보기는 고정 가격 합계와 수동 입력 옵션을 분리한다")
  void previewsFixedAmountAndManualOptions() {
    var preview = calculator.preview(answers(38000));

    assertEquals(38000, preview.baseAmount());
    assertEquals(1, preview.unconfirmedOptions().size());
    var option = preview.unconfirmedOptions().getFirst();
    assertEquals(MANUAL_GROUP_ID, option.optionGroupId());
    assertEquals("custom-size", option.optionValue());
    assertEquals("사이즈", option.label());
    assertEquals("맞춤 사이즈", option.displayValue());
    assertEquals("문의 필요", option.priceLabel());
  }

  @Test
  @DisplayName("고정 가격과 확정 가격을 합산한다")
  void calculatesFixedAndConfirmedAmount() {
    long amount = calculator.calculate(
        answers(38000), List.of(new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", 3000L)));

    assertEquals(41000, amount);
  }

  @Test
  @DisplayName("여러 문의 옵션의 확정 가격을 각각 합산한다")
  void calculatesMultipleConfirmedAmounts() {
    long amount = calculator.calculate(
        answersWithTwoManualOptions(),
        List.of(
            new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", 3000L),
            new ConfirmedOptionPrice(SECOND_MANUAL_GROUP_ID, "custom-decoration", 2000L)));

    assertEquals(43000, amount);
  }

  @Test
  @DisplayName("개별 문의 옵션의 확정 가격은 0원일 수 있다")
  void allowsZeroConfirmedAmount() {
    long amount = calculator.calculate(
        answers(38000), List.of(new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", 0L)));

    assertEquals(38000, amount);
  }

  @Test
  @DisplayName("필요한 확정 가격이 누락되면 거절한다")
  void rejectsMissingConfirmedAmount() {
    BaseException exception =
        assertThrows(BaseException.class, () -> calculator.calculate(answers(38000), List.of()));

    assertEquals(
        OrderConfirmationErrorCode.ORDER_CONFIRMATION_AMOUNT_UNCONFIRMED, exception.getErrorCode());
  }

  @Test
  @DisplayName("같은 옵션의 확정 가격이 중복되면 거절한다")
  void rejectsDuplicateConfirmedAmount() {
    ConfirmedOptionPrice price = new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", 3000L);

    assertInvalid(() -> calculator.calculate(answers(38000), List.of(price, price)));
  }

  @Test
  @DisplayName("선택된 문의 옵션과 일치하지 않는 확정 가격은 거절한다")
  void rejectsUnknownConfirmedOption() {
    assertInvalid(() -> calculator.calculate(
        answers(38000), List.of(new ConfirmedOptionPrice(FIXED_GROUP_ID, "fixed-size", 3000L))));
  }

  @Test
  @DisplayName("음수 확정 가격은 거절한다")
  void rejectsNegativeConfirmedAmount() {
    assertInvalid(() -> calculator.calculate(
        answers(38000), List.of(new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", -1L))));
  }

  @Test
  @DisplayName("금액 합산이 long 범위를 넘으면 거절한다")
  void rejectsAmountOverflow() {
    assertInvalid(() -> calculator.calculate(
        answers(Long.MAX_VALUE),
        List.of(new ConfirmedOptionPrice(MANUAL_GROUP_ID, "custom-size", 1L))));
  }

  private void assertInvalid(org.junit.jupiter.api.function.Executable executable) {
    BaseException exception = assertThrows(BaseException.class, executable);
    assertEquals(
        OrderConfirmationErrorCode.ORDER_CONFIRMATION_AMOUNT_INVALID, exception.getErrorCode());
  }

  private String answers(long fixedAmount) {
    return """
        [
          {
            "optionGroupId": "__FIXED_GROUP_ID__",
            "label": "크기",
            "selectedOptions": [
              {
                "label": "기본 크기",
                "value": "fixed-size",
                "price": __FIXED_AMOUNT__,
                "priceLabel": null
              }
            ]
          },
          {
            "optionGroupId": "__MANUAL_GROUP_ID__",
            "label": "사이즈",
            "selectedOptions": [
              {
                "label": "맞춤 사이즈",
                "value": "custom-size",
                "price": null,
                "priceLabel": "문의 필요"
              }
            ]
          }
        ]
        """.replace("__FIXED_GROUP_ID__", FIXED_GROUP_ID.toString())
        .replace("__FIXED_AMOUNT__", Long.toString(fixedAmount))
        .replace("__MANUAL_GROUP_ID__", MANUAL_GROUP_ID.toString());
  }

  private String answersWithTwoManualOptions() {
    String firstTwoAnswers = answers(38000).trim();
    return firstTwoAnswers.substring(0, firstTwoAnswers.length() - 1)
        + """
        ,
          {
            "optionGroupId": "__SECOND_MANUAL_GROUP_ID__",
            "label": "장식",
            "selectedOptions": [
              {
                "label": "맞춤 장식",
                "value": "custom-decoration",
                "price": null,
                "priceLabel": "문의 필요"
              }
            ]
          }
        ]
        """.replace("__SECOND_MANUAL_GROUP_ID__", SECOND_MANUAL_GROUP_ID.toString());
  }
}
