package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record AnswerServiceInquiryCommand(
    UUID serviceInquiryId, UUID operatorUserId, String answer) {

  public static AnswerServiceInquiryCommand of(
      UUID serviceInquiryId, UUID operatorUserId, String answer) {
    return new AnswerServiceInquiryCommand(serviceInquiryId, operatorUserId, answer);
  }
}
