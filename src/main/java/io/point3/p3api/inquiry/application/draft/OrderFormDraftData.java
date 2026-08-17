package io.point3.p3api.inquiry.application.draft;

import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record OrderFormDraftData(
        UUID storeId,
        UUID orderFormTemplateId,
        LocalDate pickupDate,
        LocalTime pickupTime,
        boolean noticeAgreed,
        List<CreateOrderFormDraftCommand.FormAnswer> formAnswers,
        List<UUID> referenceAssetIds
) {

    public static OrderFormDraftData from(CreateOrderFormDraftCommand command) {
        return new OrderFormDraftData(
                command.storeId(),
                command.orderFormTemplateId(),
                command.pickupDate(),
                command.pickupTime(),
                command.noticeAgreed(),
                command.formAnswers(),
                command.referenceAssetIds());
    }
}