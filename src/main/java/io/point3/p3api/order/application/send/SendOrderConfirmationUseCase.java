package io.point3.p3api.order.application.send;

import io.point3.p3api.order.application.result.SendOrderConfirmationResult;

public interface SendOrderConfirmationUseCase {

  SendOrderConfirmationResult send(SendOrderConfirmationCommand command);
}
