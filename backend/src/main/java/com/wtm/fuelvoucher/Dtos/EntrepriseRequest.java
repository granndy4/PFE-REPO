package com.wtm.fuelvoucher.Dtos;

import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntrepriseRequest(
        @NotNull Long societeId,
        @NotBlank @Size(max = 50) String codeEntreprise,
        @NotBlank @Size(max = 200) String raisonSociale,
        @Size(max = 120) String nomCourt,
        @Size(max = 80) String matriculeFiscal,
        String adresseFacturation,
        @Size(max = 120) String nomContact,
        @Email @Size(max = 160) String emailContact,
        @Size(max = 40) String telephoneContact,
        EntrepriseStatut statut
) {
}




