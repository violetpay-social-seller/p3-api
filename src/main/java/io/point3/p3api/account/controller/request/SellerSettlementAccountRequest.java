package io.point3.p3api.account.controller.request;

import io.point3.p3api.account.application.settlement.RegisterSellerSettlementAccountCommand;
import io.point3.p3api.account.domain.type.AccountHolderType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.UUID;

public record SellerSettlementAccountRequest(
    @NotBlank String bankCode,
    @NotBlank String accountNumber,
    @NotBlank String accountHolderName,
    @NotNull AccountHolderType holderType,
    @Past LocalDate birthDate,
    String businessRegistrationNumber) {

  @AssertTrue
  public boolean isHolderInformationValid() {
    if (holderType == null) {
      return true;
    }
    return switch (holderType) {
      case PERSONAL -> birthDate != null && businessRegistrationNumber == null;
      case BUSINESS ->
        birthDate == null
            && businessRegistrationNumber != null
            && businessRegistrationNumber
                .replace("-", "")
                .replaceAll("\\s+", "")
                .matches("[0-9]{10}");
    };
  }

  public RegisterSellerSettlementAccountCommand toCommand(UUID storeId) {
    return new RegisterSellerSettlementAccountCommand(
        storeId,
        bankCode,
        accountNumber,
        accountHolderName,
        holderType,
        birthDate,
        businessRegistrationNumber);
  }
}
