package io.point3.p3api.store.application.slug;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreSlugGenerator {

  private static final String HANGUL_SLUG_PREFIX = "wihada-";
  private static final String RANDOM_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
  private static final int RANDOM_SUFFIX_LENGTH = 8;
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String base(String name) {
    if (name == null || name.isBlank()) {
      return "store";
    }

    String normalized = Normalizer.normalize(name, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);

    if (containsHangul(normalized)) {
      return HANGUL_SLUG_PREFIX + randomSuffix();
    }

    String slug = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");

    if (slug.isBlank()) {
      return "store";
    }
    return slug;
  }

  private static boolean containsHangul(String value) {
    return value.codePoints()
        .anyMatch(
            codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HANGUL);
  }

  private static String randomSuffix() {
    StringBuilder suffix = new StringBuilder(RANDOM_SUFFIX_LENGTH);
    for (int i = 0; i < RANDOM_SUFFIX_LENGTH; i++) {
      suffix.append(RANDOM_ALPHABET.charAt(RANDOM.nextInt(RANDOM_ALPHABET.length())));
    }
    return suffix.toString();
  }
}
