package com.wtm.fuelvoucher.Dtos;

import com.wtm.fuelvoucher.Enums.ContratStatut;

import jakarta.validation.constraints.NotNull;

public record ContratStatutRequest(@NotNull ContratStatut statut) {
}



