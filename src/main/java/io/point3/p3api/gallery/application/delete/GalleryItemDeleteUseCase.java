package io.point3.p3api.gallery.application.delete;

import java.util.UUID;

public interface GalleryItemDeleteUseCase {

  void delete(UUID storeId, UUID galleryItemId);
}
