package io.point3.p3api.inquiry.application.chat.seller;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import java.util.UUID;

public interface SellerInquiryChatUseCase {

  SendChatMessageResult sendMessage(UUID inquiryId, UUID storeId, UUID sellerUserId, String content);

  ChatTimelinePage getTimeline(UUID inquiryId, UUID storeId, ChatTimelineQuery query);
}
