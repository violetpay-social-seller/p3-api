package io.point3.p3api.account.application.settlement;

import io.point3.p3api.account.domain.type.AccountHolderType;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterSellerSettlementAccountCommand(
    UUID storeId,
    String bankCode,
    String accountNumber,
    String accountHolderName,
    AccountHolderType holderType,
    LocalDate birthDate,
    String businessRegistrationNumber) {}
