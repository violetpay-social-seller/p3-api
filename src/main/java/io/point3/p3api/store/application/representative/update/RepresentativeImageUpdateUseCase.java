package io.point3.p3api.store.application.representative.update;

import io.point3.p3api.store.application.representative.command.UpdateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;

public interface RepresentativeImageUpdateUseCase {
  RepresentativeImageResult update(UpdateRepresentativeImageCommand command);
}
