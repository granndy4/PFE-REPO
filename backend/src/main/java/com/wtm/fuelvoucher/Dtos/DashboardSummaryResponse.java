package com.wtm.fuelvoucher.Dtos;

public record DashboardSummaryResponse(
        long entreprisesTotal,
        long entreprisesActive,
        long entreprisesSuspended,
        long contratsTotal,
        long contratsActive,
        long vehiculesTotal,
        long vehiculesActifs,
        long employesTotal,
        long employesActifs,
        long bonsTotal,
        long bonsIssued,
        long bonsPartiallyConsumed,
        long bonsConsumed,
        long bonsRegenerated,
        long consommationsTotal,
        long auditsTotal
) {
}