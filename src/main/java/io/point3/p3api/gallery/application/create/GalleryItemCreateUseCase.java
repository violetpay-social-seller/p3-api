package io.point3.p3api.gallery.application.create;

import io.point3.p3api.gallery.application.command.CreateGalleryItemCommand;
import io.point3.p3api.gallery.application.result.GalleryItemResult;

public interface GalleryItemCreateUseCase {

  GalleryItemResult create(CreateGalleryItemCommand command);
}
