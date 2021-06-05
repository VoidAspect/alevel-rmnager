package com.alevel.rmanager.data.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveManagedResourceRequest(
        @NotBlank
        String name,
        String description,
        @Min(1)
        int totalCapacity
) {
}