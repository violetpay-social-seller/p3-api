package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record CloseServiceInquiryCommand(UUID serviceInquiryId, UUID operatorUserId) {

  public static CloseServiceInquiryCommand of(UUID serviceInquiryId, UUID operatorUserId) {
    return new CloseServiceInquiryCommand(serviceInquiryId, operatorUserId);
  }
}
