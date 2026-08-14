package io.point3.p3api.chat.application.timeline.query;

import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import java.util.UUID;

public interface ChatTimelineQueryUseCase {

  ChatTimelinePage execute(UUID inquiryId, ChatTimelineQuery query);
}
