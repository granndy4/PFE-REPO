package com.wtm.fuelvoucher.contrat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.wtm.fuelvoucher.contrat.ContratStatut;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContratRequest(
        @NotNull Long societeId,
        @NotNull Long entrepriseId,
        @NotBlank @Size(max = 60) String numeroContrat,
        @NotNull LocalDate dateDebut,
        LocalDate dateFin,
        @Size(min = 3, max = 3) String codeDevise,
        @Min(0) Integer delaiPaiementJours,
        @DecimalMin(value = "0.000") BigDecimal montantMaxMensuel,
        ContratStatut statut,
        LocalDate signeLe,
        String notes
) {
}