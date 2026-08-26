package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record CreateServiceInquiryCommand(UUID requesterUserId, String title, String body) {

  public static CreateServiceInquiryCommand of(UUID requesterUserId, String title, String body) {
    return new CreateServiceInquiryCommand(requesterUserId, title, body);
  }
}
