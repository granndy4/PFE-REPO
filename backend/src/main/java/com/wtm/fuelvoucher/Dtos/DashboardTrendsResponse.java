package com.wtm.fuelvoucher.Dtos;

import java.util.List;

public record DashboardTrendsResponse(
        Long societeId,
        String fromPeriod,
        String toPeriod,
        List<DashboardTrendPointResponse> points
) {
}