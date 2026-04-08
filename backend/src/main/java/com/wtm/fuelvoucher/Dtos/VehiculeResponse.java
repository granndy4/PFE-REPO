package com.wtm.fuelvoucher.Dtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.wtm.fuelvoucher.Enums.TypeCarburant;

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




