package com.wtm.fuelvoucher.Dtos;

import java.time.Instant;

public record EmployeResponse(
        Long id,
        Long societeId,
        Long entrepriseId,
        String codeEmploye,
        String nomComplet,
        String cin,
        String telephone,
        String email,
        String poste,
        boolean actif,
        Instant creeLe,
        Instant modifieLe
) {
}




