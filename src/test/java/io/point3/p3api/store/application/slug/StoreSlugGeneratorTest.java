package io.point3.p3api.store.application.slug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StoreSlugGeneratorTest {

  @Test
  @DisplayName("영문 스토어명은 읽을 수 있는 소문자 슬러그로 변환한다")
  void createsReadableAsciiSlug() {
    String slug = StoreSlugGenerator.base("Minseo Cake 101");

    assertEquals("minseo-cake-101", slug);
  }

  @Test
  @DisplayName("한글 스토어명은 위하다 랜덤 슬러그로 변환한다")
  void createsRandomSlugForHangulName() {
    String slug = StoreSlugGenerator.base("민서 케이크");

    assertTrue(slug.matches("wihada-[a-z0-9]{8}"));
    assertFalse(containsHangul(slug));
  }

  @Test
  @DisplayName("영문과 한글이 섞이면 위하다 랜덤 슬러그로 변환한다")
  void createsRandomSlugForMixedHangulName() {
    String slug = StoreSlugGenerator.base("P3 베이커리");

    assertTrue(slug.matches("wihada-[a-z0-9]{8}"));
    assertFalse(containsHangul(slug));
  }

  @Test
  @DisplayName("슬러그로 변환할 문자가 없으면 기본 슬러그를 사용한다")
  void usesDefaultSlugWhenNameHasNoSlugText() {
    String slug = StoreSlugGenerator.base("!!!");

    assertEquals("store", slug);
  }

  private boolean containsHangul(String value) {
    return value.codePoints()
        .anyMatch(
            codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HANGUL);
  }
}
