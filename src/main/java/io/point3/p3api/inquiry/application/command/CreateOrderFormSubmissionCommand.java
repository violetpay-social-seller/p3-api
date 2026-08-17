package io.point3.p3api.inquiry.application.command;

import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record CreateOrderFormSubmissionCommand(
        UUID storeId,
        UUID buyerUserId,
        UUID inquiryId,
        UUID orderFormTemplateId,
        List<FormAnswer> formAnswers,
        SubmissionContext submissionContext
) {
    public static CreateOrderFormSubmissionCommand of(
            UUID storeId,
            UUID buyerUserId,
            UUID inquiryId,
            UUID orderFormTemplateId,
            List<FormAnswer> formAnswers,
            SubmissionContext submissionContext) {
        return new CreateOrderFormSubmissionCommand(
                storeId,
                buyerUserId,
                inquiryId,
                orderFormTemplateId,
                formAnswers,
                submissionContext == null ? SubmissionContext.none() : submissionContext);
    }
    public record FormAnswer(UUID fieldId, JsonNode value) {}
    public UUID productId() {
        return submissionContext.productId();
    }
    public List<ProductOptionSelection> productOptionSelections() {
        return submissionContext.productOptionSelections();
    }
    // Product 도메인 기획상 변경여지가 있어 옵셔널하게 넣어둠
    public record SubmissionContext(
            UUID productId,
            List<ProductOptionSelection> productOptionSelections) {
        public static SubmissionContext none() {
            return new SubmissionContext(null, List.of());
        }
        public static SubmissionContext product(
                UUID productId, List<ProductOptionSelection> productOptionSelections) {
            return new SubmissionContext(
                    productId, productOptionSelections == null ? List.of() : productOptionSelections);
        }
        public boolean hasProduct() {
            return productId != null;
        }
    }
    public record ProductOptionSelection(UUID optionGroupId, List<UUID> optionIds) {}
}
