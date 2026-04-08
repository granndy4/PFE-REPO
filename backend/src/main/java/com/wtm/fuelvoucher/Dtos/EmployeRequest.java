package com.wtm.fuelvoucher.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeRequest(
        @NotNull Long societeId,
        @NotNull Long entrepriseId,
        @NotBlank @Size(max = 50) String codeEmploye,
        @NotBlank @Size(max = 140) String nomComplet,
        @Size(max = 40) String cin,
        @Size(max = 40) String telephone,
        @Email @Size(max = 160) String email,
        @Size(max = 100) String poste,
        Boolean actif
) {
}




