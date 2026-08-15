package io.point3.p3api.inquiry.application.command;

import java.util.UUID;

public record OpenInquiryCommand(UUID storeId, UUID buyerUserId, UUID contextProductId) {}
