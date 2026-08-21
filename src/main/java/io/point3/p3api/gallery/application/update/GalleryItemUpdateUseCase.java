package io.point3.p3api.gallery.application.update;

import io.point3.p3api.gallery.application.command.UpdateGalleryItemCommand;
import io.point3.p3api.gallery.application.result.GalleryItemResult;

public interface GalleryItemUpdateUseCase {

  GalleryItemResult update(UpdateGalleryItemCommand command);
}
