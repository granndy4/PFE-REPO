package com.wtm.fuelvoucher.Dtos;

import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

import jakarta.validation.constraints.NotNull;

public record EntrepriseStatutRequest(@NotNull EntrepriseStatut statut) {
}




