package io.point3.p3api.inquiry.application.open;

import java.util.UUID;

public record OpenInquiryCommand(
        UUID storeId,
        UUID buyerUserId,
        UUID contextProductId
) {
}
