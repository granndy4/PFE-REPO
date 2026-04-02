package com.wtm.fuelvoucher.employe.dto;

import jakarta.validation.constraints.NotNull;

public record EmployeActifRequest(@NotNull Boolean actif) {
}
