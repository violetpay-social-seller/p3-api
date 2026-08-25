package io.point3.p3api.dashboard.application.query;

import io.point3.p3api.dashboard.application.result.SellerDashboardResult;
import io.point3.p3api.dashboard.application.result.SellerRevenueResult;

public interface SellerDashboardQueryUseCase {

  SellerDashboardResult getSummary(SellerDashboardQueryCommand command);

  SellerRevenueResult getRevenue(SellerRevenueQueryCommand command);
}
