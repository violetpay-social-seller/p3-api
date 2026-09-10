package io.point3.p3api.common.tenant.seller.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.common.tenant.seller.provider.SellerOnboardingApprovalProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SellerOnboardingApprovalFilterTest {

  private final SellerOnboardingApprovalFilter filter = new SellerOnboardingApprovalFilter(
      mock(CurrentUserRender.class),
      mock(SellerOnboardingApprovalProvider.class),
      mock(ObjectMapper.class));

  @Test
  void skipsApprovalCheckForStoreLocationSearch() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/seller/store/locations/search");

    assertTrue(filter.shouldNotFilter(request));
  }

  @Test
  void keepsApprovalCheckForOtherSellerRequests() {
    MockHttpServletRequest storeRequest = new MockHttpServletRequest("GET", "/seller/store");
    MockHttpServletRequest locationSearchPostRequest =
        new MockHttpServletRequest("POST", "/seller/store/locations/search");

    assertFalse(filter.shouldNotFilter(storeRequest));
    assertFalse(filter.shouldNotFilter(locationSearchPostRequest));
  }
}
