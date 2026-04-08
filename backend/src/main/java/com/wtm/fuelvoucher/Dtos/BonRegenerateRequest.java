package com.wtm.fuelvoucher.Dtos;

import jakarta.validation.constraints.Size;

public record BonRegenerateRequest(
        @Size(max = 255) String motif
) {
}
