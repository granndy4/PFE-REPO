package com.wtm.fuelvoucher.contrat.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.wtm.fuelvoucher.contrat.ContratStatut;

public record ContratResponse(
        Long id,
        Long societeId,
        Long entrepriseId,
        String numeroContrat,
        LocalDate dateDebut,
        LocalDate dateFin,
        String codeDevise,
        Integer delaiPaiementJours,
        BigDecimal montantMaxMensuel,
        ContratStatut statut,
        LocalDate signeLe,
        String notes,
        Long creeParUtilisateurId,
        Instant creeLe,
        Instant modifieLe
) {
}