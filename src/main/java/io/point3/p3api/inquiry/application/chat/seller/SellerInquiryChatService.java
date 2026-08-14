package io.point3.p3api.inquiry.application.chat.seller;

import io.point3.p3api.chat.application.send.SendChatMessageCommand;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.send.SendChatMessageUseCase;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQueryUseCase;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerInquiryChatService implements SellerInquiryChatUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final SendChatMessageUseCase sendChatMessageUseCase;
  private final ChatTimelineQueryUseCase chatTimelineQueryUseCase;

  @Override
  public SendChatMessageResult sendMessage(
      UUID inquiryId, UUID storeId, UUID sellerUserId, String content) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    return sendChatMessageUseCase.execute(new SendChatMessageCommand(inquiryId, sellerUserId, content));
  }

  @Override
  public ChatTimelinePage getTimeline(UUID inquiryId, UUID storeId, ChatTimelineQuery query) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    return chatTimelineQueryUseCase.execute(inquiryId, query);
  }
}
