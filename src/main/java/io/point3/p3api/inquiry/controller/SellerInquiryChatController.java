package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.chat.SellerInquiryChatUseCase;
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
@RequestMapping("/seller/inquiries/{inquiryId}")
@RequiredArgsConstructor
public class SellerInquiryChatController {

  private final SellerInquiryChatUseCase sellerInquiryChatUseCase;

  @GetMapping
  public ApiResponse<InquiryChatDetailResponse> getDetail(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        InquiryChatDetailResponse.from(sellerInquiryChatUseCase.getDetail(inquiryId, storeId)));
  }

  @GetMapping("/events")
  public ApiResponse<ChatTimelinePageResponse> getTimeline(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId,
      @RequestParam(required = false) Instant cursorCreatedAt,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) Integer size) {
    ChatTimelinePage page = sellerInquiryChatUseCase.getTimeline(
        inquiryId, storeId, new ChatTimelineQuery(cursorCreatedAt, cursorId, size));
    return ApiResponse.ok(ChatTimelinePageResponse.from(page));
  }
}
