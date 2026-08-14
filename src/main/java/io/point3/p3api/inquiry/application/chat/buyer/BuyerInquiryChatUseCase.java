package io.point3.p3api.inquiry.application.chat.buyer;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.inquiry.application.chat.detail.InquiryChatDetail;
import java.util.UUID;

public interface BuyerInquiryChatUseCase {

  InquiryChatDetail getDetail(UUID inquiryId, UUID buyerUserId);

  SendChatMessageResult sendMessage(UUID inquiryId, UUID buyerUserId, String content);

  ChatTimelinePage getTimeline(UUID inquiryId, UUID buyerUserId, ChatTimelineQuery query);
}
