package com.wtm.fuelvoucher.vehicule.dto;

import java.math.BigDecimal;

import com.wtm.fuelvoucher.vehicule.TypeCarburant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehiculeRequest(
        @NotNull Long societeId,
        @NotNull Long entrepriseId,
        Long employeId,
        @NotBlank @Size(max = 40) String immatriculation,
        @Size(max = 50) String codeFlotte,
        @Size(max = 60) String marque,
        @Size(max = 60) String modele,
        @NotNull TypeCarburant typeCarburant,
        @Positive BigDecimal capaciteReservoirLitres,
        Boolean actif
) {
}
