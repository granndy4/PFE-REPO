package com.wtm.fuelvoucher.vehicule.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.wtm.fuelvoucher.vehicule.TypeCarburant;

public record VehiculeResponse(
        Long id,
        Long societeId,
        Long entrepriseId,
        Long employeId,
        String immatriculation,
        String codeFlotte,
        String marque,
        String modele,
        TypeCarburant typeCarburant,
        BigDecimal capaciteReservoirLitres,
        boolean actif,
        Instant creeLe,
        Instant modifieLe
) {
}
