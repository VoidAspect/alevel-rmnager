package com.alevel.rmanager.data.model.dto;

public record ManagedResourceRecord(
        Long id,
        String name,
        String description,
        int capacity,
        int totalCapacity
) {
}
