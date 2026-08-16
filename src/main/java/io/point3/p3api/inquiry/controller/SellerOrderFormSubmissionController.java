package io.point3.p3api.inquiry.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.order.controller.response.OrderFormSubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}/order-form-submissions")
@RequiredArgsConstructor
public class SellerOrderFormSubmissionController {

    private final InquiryChatAccessService inquiryChatAccessService;
    private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;

    @GetMapping("/{submissionId}")
    public ApiResponse<OrderFormSubmissionResponse> getSubmission(
            @PathVariable UUID inquiryId,
            @PathVariable UUID submissionId,
            @CurrentStoreId UUID storeId) {
        Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);

        OrderFormSubmission submission = orderFormSubmissionPersistencePort
                .findById(submissionId)
                .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));

        if (!submission.getInquiryId().equals(inquiry.getId())) {
            throw new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND);
        }

        return ApiResponse.ok(OrderFormSubmissionResponse.from(submission));
    }

    @GetMapping
    public ApiResponse<List<OrderFormSubmissionResponse>> getSubmissions(
            @PathVariable UUID inquiryId,
            @CurrentStoreId UUID storeId) {
        Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);

        List<OrderFormSubmission> submissions = orderFormSubmissionPersistencePort.findAllByInquiryId(inquiryId);

        return ApiResponse.ok(submissions.stream()
                .map(OrderFormSubmissionResponse::from)
                .toList());

    }
}
