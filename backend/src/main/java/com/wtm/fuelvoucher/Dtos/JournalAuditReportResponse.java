package com.wtm.fuelvoucher.Dtos;

import java.time.Instant;

public record JournalAuditReportResponse(
        Long id,
        Long societeId,
        Long utilisateurId,
        String typeEvenement,
        String nomEntite,
        Long idEntite,
        String action,
        String adresseIp,
        String agentUtilisateur,
        String anciennesValeursJson,
        String nouvellesValeursJson,
        Instant creeLe
) {
}