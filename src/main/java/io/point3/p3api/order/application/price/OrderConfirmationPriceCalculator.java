package io.point3.p3api.order.application.price;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConfirmationPriceCalculator {

  private final ObjectMapper objectMapper;

  public PricePreview preview(String answers) {
    long baseAmount = 0;
    java.util.ArrayList<UnconfirmedOption> unconfirmedOptions = new java.util.ArrayList<>();
    try {
      for (JsonNode answer : objectMapper.readTree(answers)) {
        baseAmount = Math.addExact(baseAmount, readSnapshotPrice(answer.path("price")));
        UUID optionGroupId = readOptionGroupId(answer);
        for (JsonNode option : answer.path("selectedOptions")) {
          if (requiresManualAmount(option)) {
            unconfirmedOptions.add(new UnconfirmedOption(
                optionGroupId,
                requiredText(option.path("value")),
                text(answer.path("label")),
                firstText(option.path("text"), option.path("label"), option.path("value")),
                requiredText(option.path("priceLabel"))));
          } else {
            baseAmount = Math.addExact(baseAmount, readSnapshotPrice(option.path("price")));
          }
        }
      }
    } catch (JsonProcessingException | ArithmeticException exception) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    return new PricePreview(baseAmount, unconfirmedOptions);
  }

  public long calculate(String answers, List<ConfirmedOptionPrice> confirmedOptionPrices) {
    PricePreview preview = preview(answers);
    Map<OptionKey, Long> confirmedAmounts = validatedConfirmedAmounts(confirmedOptionPrices);
    Set<OptionKey> expectedKeys = preview.unconfirmedOptions().stream()
        .map(option -> new OptionKey(option.optionGroupId(), option.optionValue()))
        .collect(java.util.stream.Collectors.toCollection(HashSet::new));
    if (!expectedKeys.containsAll(confirmedAmounts.keySet())) {
      throw invalidAmount();
    }
    long amount = preview.baseAmount();

    try {
      for (UnconfirmedOption option : preview.unconfirmedOptions()) {
        OptionKey key = new OptionKey(option.optionGroupId(), option.optionValue());
        Long confirmedAmount = confirmedAmounts.get(key);
        if (confirmedAmount == null) {
          throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_AMOUNT_UNCONFIRMED);
        }
        amount = Math.addExact(amount, confirmedAmount);
      }
    } catch (ArithmeticException exception) {
      throw invalidAmount();
    }

    return amount;
  }

  private Map<OptionKey, Long> validatedConfirmedAmounts(
      List<ConfirmedOptionPrice> confirmedOptionPrices) {
    Map<OptionKey, Long> amounts = new HashMap<>();
    for (ConfirmedOptionPrice confirmedPrice : confirmedOptionPrices) {
      if (confirmedPrice == null
          || confirmedPrice.optionGroupId() == null
          || isBlank(confirmedPrice.optionValue())
          || confirmedPrice.amount() == null
          || confirmedPrice.amount() < 0) {
        throw invalidAmount();
      }
      OptionKey key = new OptionKey(confirmedPrice.optionGroupId(), confirmedPrice.optionValue());
      if (amounts.putIfAbsent(key, confirmedPrice.amount()) != null) {
        throw invalidAmount();
      }
    }
    return amounts;
  }

  private UUID readOptionGroupId(JsonNode answer) {
    try {
      return UUID.fromString(requiredText(answer.path("optionGroupId")));
    } catch (IllegalArgumentException exception) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private long readSnapshotPrice(JsonNode price) {
    if (price.isMissingNode() || price.isNull()) {
      return 0;
    }
    if (!price.canConvertToLong() || price.asLong() < 0) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    return price.asLong();
  }

  private boolean requiresManualAmount(JsonNode option) {
    return option.path("priceLabel").isTextual()
        && !option.path("priceLabel").asText().isBlank();
  }

  private String firstText(JsonNode... nodes) {
    for (JsonNode node : nodes) {
      String value = text(node);
      if (!isBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private String requiredText(JsonNode node) {
    String value = text(node);
    if (isBlank(value)) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    return value;
  }

  private String text(JsonNode node) {
    return node != null && node.isTextual() ? node.asText() : null;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private BaseException invalidAmount() {
    return new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_AMOUNT_INVALID);
  }

  private record OptionKey(UUID optionGroupId, String optionValue) {}

  public record PricePreview(long baseAmount, List<UnconfirmedOption> unconfirmedOptions) {

    public PricePreview {
      unconfirmedOptions = List.copyOf(unconfirmedOptions);
    }

    @Override
    public List<UnconfirmedOption> unconfirmedOptions() {
      return List.copyOf(unconfirmedOptions);
    }
  }

  public record UnconfirmedOption(
      UUID optionGroupId,
      String optionValue,
      String label,
      String displayValue,
      String priceLabel) {}
}
