package com.wtm.fuelvoucher.Dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BonConsumeRequest(
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantiteLitres,
        @NotBlank @Size(max = 120) String referenceTransaction,
        @Size(max = 255) String notes
) {
}
