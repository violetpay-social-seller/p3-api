package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQueryUseCase;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyerInquiryChatService implements BuyerInquiryChatUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final InquiryChatDetailQueryUseCase inquiryChatDetailQueryUseCase;
  private final ChatTimelineQueryUseCase chatTimelineQueryUseCase;

  @Override
  public InquiryChatDetail getDetail(UUID inquiryId, UUID buyerUserId) {
    return inquiryChatDetailQueryUseCase.getBuyerDetail(
        inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId));
  }

  @Override
  public ChatTimelinePage getTimeline(UUID inquiryId, UUID buyerUserId, ChatTimelineQuery query) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return chatTimelineQueryUseCase.execute(inquiryId, query);
  }
}
