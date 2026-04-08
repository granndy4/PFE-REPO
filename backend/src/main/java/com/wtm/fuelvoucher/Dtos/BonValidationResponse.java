package com.wtm.fuelvoucher.Dtos;

import com.wtm.fuelvoucher.Enums.BonStatut;

import java.math.BigDecimal;

public record BonValidationResponse(
        boolean valide,
        String message,
        Long bonId,
        String referenceBon,
        BonStatut statut,
        BigDecimal soldeLitres,
        String qrCodePayload
) {
}
