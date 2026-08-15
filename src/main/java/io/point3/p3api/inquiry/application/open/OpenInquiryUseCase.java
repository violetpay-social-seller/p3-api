package io.point3.p3api.inquiry.application.open;

import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.domain.entity.Inquiry;

public interface OpenInquiryUseCase {

  Inquiry open(OpenInquiryCommand command);
}
