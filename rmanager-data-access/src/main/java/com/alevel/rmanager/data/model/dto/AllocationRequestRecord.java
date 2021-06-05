package com.alevel.rmanager.data.model.dto;

import com.alevel.rmanager.data.model.entity.AllocationResult;

import java.time.Instant;

public record AllocationRequestRecord(
        Long id,
        Long resourceId,
        int capacity,
        int previousResourceCapacity,
        Instant issuedAt,
        AllocationResultRecord result
) {

    public AllocationRequestRecord(Long id,
                                   Long resourceId,
                                   int capacity,
                                   int previousResourceCapacity,
                                   Instant issuedAt,
                                   AllocationResult.Status status,
                                   String reason) {

        this(id, resourceId, capacity, previousResourceCapacity, issuedAt, new AllocationResultRecord(status, reason));
    }
}
