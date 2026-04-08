package com.wtm.fuelvoucher.Dtos;

import java.time.Instant;

import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

public record EntrepriseResponse(
        Long id,
        Long societeId,
        String codeEntreprise,
        String raisonSociale,
        String nomCourt,
        String matriculeFiscal,
        String adresseFacturation,
        String nomContact,
        String emailContact,
        String telephoneContact,
        EntrepriseStatut statut,
        Instant creeLe,
        Instant modifieLe
) {
}




