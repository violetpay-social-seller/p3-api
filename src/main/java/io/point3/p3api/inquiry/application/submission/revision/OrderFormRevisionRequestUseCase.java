package io.point3.p3api.inquiry.application.submission.revision;

import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.inquiry.application.command.RequestOrderFormRevisionCommand;

public interface OrderFormRevisionRequestUseCase {

  ChatTimelineItemResult requestRevision(RequestOrderFormRevisionCommand command);
}
