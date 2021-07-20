package com.alevel.rmanager.data.service;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.exception.RManagerDataLayerException;
import com.alevel.rmanager.data.model.dto.AllocationResultRecord;
import com.alevel.rmanager.data.model.dto.ManagedResourceRecord;
import com.alevel.rmanager.data.model.dto.SaveManagedResourceRequest;

import java.util.Optional;

public interface ManagedResourceService {

    Optional<ManagedResourceRecord> getById(long id);

    ManagedResourceRecord save(SaveManagedResourceRequest managedResource) throws RManagerDataLayerException;

    void update(long id, SaveManagedResourceRequest managedResource) throws RManagerDataLayerException, ManagedResourceNotFoundException;

    void delete(long id) throws ManagedResourceNotFoundException;

    AllocationResultRecord allocate(long id, int units) throws ManagedResourceNotFoundException, RManagerDataLayerException;

}
