package io.point3.p3api.inquiry.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFormSubmissionCommand(
        UUID storeId,
        UUID buyerUserId,
        UUID inquiryId,
        UUID orderFormTemplateId,
        List<FormAnswer> formAnswers,
        PickupRequest pickupRequest,
        NoticeAgreement noticeAgreement,
        SubmissionReference reference
) {
    public record FormAnswer(UUID fieldId, JsonNode value) {}

    public record PickupRequest(
            LocalDate pickupDate,
            LocalTime pickupTime
    ) {}

    public record NoticeAgreement(
            boolean agreed
    ) {}

    public record SubmissionReference(
            List<UUID> referenceAssetIds
    ) {
        public static SubmissionReference empty() {
            return new SubmissionReference(List.of());
        }
    }
}