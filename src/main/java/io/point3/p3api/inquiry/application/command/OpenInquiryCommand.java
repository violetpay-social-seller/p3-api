package io.point3.p3api.inquiry.application.command;

import java.util.UUID;

// TODO: contextProductId 는 새로운기획(상품제거 -> 주문서통합)에서 삭제될 가능성 높음
public record OpenInquiryCommand(UUID storeId, UUID buyerUserId, UUID contextProductId) {}
