package com.wtm.fuelvoucher.Dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BonCreateRequest(
        @NotNull Long societeId,
        @NotNull Long entrepriseId,
        Long contratId,
        @NotNull Long vehiculeId,
        Long employeId,
        @Size(max = 80) String referenceBon,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantiteInitialeLitres,
        @NotBlank @Size(max = 120) String referenceTransactionInitiale,
        @Size(max = 255) String notes
) {
}
