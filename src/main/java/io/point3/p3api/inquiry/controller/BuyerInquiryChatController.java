package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.chat.BuyerInquiryChatUseCase;
import io.point3.p3api.inquiry.controller.response.ChatTimelinePageResponse;
import io.point3.p3api.inquiry.controller.response.InquiryChatDetailResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inquiries/{inquiryId}")
@RequiredArgsConstructor
public class BuyerInquiryChatController {

  private final BuyerInquiryChatUseCase buyerInquiryChatUseCase;

  @GetMapping
  public ApiResponse<InquiryChatDetailResponse> getDetail(
      @PathVariable UUID inquiryId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);

    return ApiResponse.ok(InquiryChatDetailResponse.from(
        buyerInquiryChatUseCase.getDetail(inquiryId, currentUser.userId())));
  }

  @GetMapping("/events")
  public ApiResponse<ChatTimelinePageResponse> getTimeline(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) Instant cursorCreatedAt,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireBuyer(currentUser);

    ChatTimelinePage page = buyerInquiryChatUseCase.getTimeline(
        inquiryId, currentUser.userId(), new ChatTimelineQuery(cursorCreatedAt, cursorId, size));
    return ApiResponse.ok(ChatTimelinePageResponse.from(page));
  }
}
