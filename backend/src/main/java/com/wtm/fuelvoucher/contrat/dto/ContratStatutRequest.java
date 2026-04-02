package com.wtm.fuelvoucher.contrat.dto;

import com.wtm.fuelvoucher.contrat.ContratStatut;

import jakarta.validation.constraints.NotNull;

public record ContratStatutRequest(@NotNull ContratStatut statut) {
}