package io.point3.p3api.store.application.representative.create;

import io.point3.p3api.store.application.representative.command.CreateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;

public interface RepresentativeImageCreateUseCase {
  RepresentativeImageResult create(CreateRepresentativeImageCommand command);
}
