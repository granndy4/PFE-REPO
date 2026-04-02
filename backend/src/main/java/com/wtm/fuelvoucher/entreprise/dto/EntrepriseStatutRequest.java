package com.wtm.fuelvoucher.entreprise.dto;

import com.wtm.fuelvoucher.entreprise.EntrepriseStatut;

import jakarta.validation.constraints.NotNull;

public record EntrepriseStatutRequest(@NotNull EntrepriseStatut statut) {
}
