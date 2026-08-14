package io.point3.p3api.inquiry.application.chat.buyer;

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
public class BuyerInquiryChatService implements BuyerInquiryChatUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final SendChatMessageUseCase sendChatMessageUseCase;
  private final ChatTimelineQueryUseCase chatTimelineQueryUseCase;

  @Override
  public SendChatMessageResult sendMessage(UUID inquiryId, UUID buyerUserId, String content) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return sendChatMessageUseCase.execute(new SendChatMessageCommand(inquiryId, buyerUserId, content));
  }

  @Override
  public ChatTimelinePage getTimeline(
      UUID inquiryId, UUID buyerUserId, ChatTimelineQuery query) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return chatTimelineQueryUseCase.execute(inquiryId, query);
  }
}
