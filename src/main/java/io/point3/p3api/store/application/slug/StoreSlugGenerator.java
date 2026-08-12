package io.point3.p3api.store.application.slug;

import java.text.Normalizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreSlugGenerator {

  public static String base(String name) {
    if (name == null || name.isBlank()) {
      return "store";
    }

    String normalized = Normalizer.normalize(name, Normalizer.Form.NFC).toLowerCase();
    String slug = normalized.replaceAll("[^a-z0-9가-힣]+", "-").replaceAll("(^-+|-+$)", "");

    if (slug.isBlank()) {
      return "store";
    }
    return slug;
  }
}
