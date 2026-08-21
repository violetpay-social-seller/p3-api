package io.point3.p3api.inquiry.application.submission.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFormSelectedGallerySnapshotFactory {

  private final ObjectMapper objectMapper;

  public String create(UUID selectedGalleryItemId) {
    if (selectedGalleryItemId == null) {
      return null;
    }

    return write(new SelectedGallerySnapshot(selectedGalleryItemId));
  }

  private String write(Object snapshot) {
    try {
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private record SelectedGallerySnapshot(UUID galleryItemId) {}
}
