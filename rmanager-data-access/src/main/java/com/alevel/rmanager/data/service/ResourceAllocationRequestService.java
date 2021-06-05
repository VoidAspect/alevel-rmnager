package com.alevel.rmanager.data.service;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.model.dto.AllocationRequestRecord;

import java.util.List;
import java.util.Optional;

public interface ResourceAllocationRequestService {

    List<AllocationRequestRecord> getByResourceId(long resourceId) throws ManagedResourceNotFoundException;

    Optional<AllocationRequestRecord> getById(long id);

}
