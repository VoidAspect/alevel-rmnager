package com.alevel.rmanager.data.model.dto;

import com.alevel.rmanager.data.model.entity.AllocationResult;

public record AllocationResultRecord(
        AllocationResult.Status status,
        String reason
) {
}
