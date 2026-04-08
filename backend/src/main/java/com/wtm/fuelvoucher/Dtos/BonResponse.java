package com.wtm.fuelvoucher.Dtos;

import com.wtm.fuelvoucher.Enums.BonStatut;

import java.math.BigDecimal;
import java.time.Instant;

public record BonResponse(
        Long id,
        Long societeId,
        Long entrepriseId,
        Long contratId,
        Long vehiculeId,
        Long employeId,
        String referenceBon,
        String qrCodePayload,
        BigDecimal quantiteInitialeLitres,
        BigDecimal soldeLitres,
        BonStatut statut,
        boolean defectueux,
        Long bonOriginalId,
        Instant creeLe,
        Instant modifieLe
) {
}
