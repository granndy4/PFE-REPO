package com.wtm.fuelvoucher.Dtos;

import java.math.BigDecimal;

public record DashboardTrendPointResponse(
        String period,
        long bonsIssued,
        long consommationsCount,
        BigDecimal quantiteConsommeeLitres,
        long auditsCount
) {
}