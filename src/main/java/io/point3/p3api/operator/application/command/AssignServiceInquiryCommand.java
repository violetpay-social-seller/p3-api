package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record AssignServiceInquiryCommand(UUID serviceInquiryId, UUID operatorUserId) {

  public static AssignServiceInquiryCommand of(UUID serviceInquiryId, UUID operatorUserId) {
    return new AssignServiceInquiryCommand(serviceInquiryId, operatorUserId);
  }
}
