package com.wtm.fuelvoucher.vehicule.dto;

import jakarta.validation.constraints.NotNull;

public record VehiculeActifRequest(@NotNull Boolean actif) {
}
