package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.result.InquiryStorePolicy;
import java.util.UUID;

public interface BuyerInquiryChatUseCase {

  InquiryChatDetail getDetail(UUID inquiryId, UUID buyerUserId);

  ChatTimelinePage getTimeline(UUID inquiryId, UUID buyerUserId, ChatTimelineQuery query);

  InquiryStorePolicy getStorePolicy(UUID inquiryId, UUID buyerUserId);
}
