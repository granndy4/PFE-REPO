package com.wtm.fuelvoucher.Dtos;

import java.math.BigDecimal;
import java.time.Instant;

public record BonConsommationReportResponse(
        Long consommationId,
        Long bonId,
        String referenceBon,
        Long societeId,
        Long entrepriseId,
        Long vehiculeId,
        String referenceTransaction,
        BigDecimal quantiteLitres,
        Instant consommeLe,
        Long consommeParUtilisateurId,
        String notes
) {
}