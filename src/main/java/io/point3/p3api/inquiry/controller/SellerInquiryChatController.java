package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.InquiryChatService;
import io.point3.p3api.inquiry.application.InquiryChatService.ChatTimelinePage;
import io.point3.p3api.inquiry.controller.request.SendChatMessageRequest;
import io.point3.p3api.inquiry.controller.response.ChatTimelineItemResponse;
import io.point3.p3api.inquiry.controller.response.ChatTimelinePageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}")
@RequiredArgsConstructor
public class SellerInquiryChatController {

  private final InquiryChatService inquiryChatService;

  @PostMapping("/messages")
  public ApiResponse<ChatTimelineItemResponse> sendMessage(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId,
      @Valid @RequestBody SendChatMessageRequest request) {
    return ApiResponse.ok(
        ChatTimelineItemResponse.from(
            inquiryChatService.sendSellerMessage(
                inquiryId, storeId, currentUser.userId(), request.content())));
  }

  @GetMapping("/events")
  public ApiResponse<ChatTimelinePageResponse> getTimeline(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId,
      @RequestParam(required = false) Instant cursorCreatedAt,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) Integer size) {
    ChatTimelinePage page = inquiryChatService.getSellerTimeline(
        inquiryId, storeId, cursorCreatedAt, cursorId, size);
    return ApiResponse.ok(ChatTimelinePageResponse.from(page));
  }
}
