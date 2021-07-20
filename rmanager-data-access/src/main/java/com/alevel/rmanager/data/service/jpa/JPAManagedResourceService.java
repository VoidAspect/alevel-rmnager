package com.alevel.rmanager.data.service.jpa;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.exception.RManagerDataLayerException;
import com.alevel.rmanager.data.model.dto.AllocationResultRecord;
import com.alevel.rmanager.data.model.dto.ManagedResourceRecord;
import com.alevel.rmanager.data.model.dto.SaveManagedResourceRequest;
import com.alevel.rmanager.data.model.entity.AllocationRequest;
import com.alevel.rmanager.data.model.entity.AllocationResult;
import com.alevel.rmanager.data.model.entity.ManagedResource;
import com.alevel.rmanager.data.service.ManagedResourceService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class JPAManagedResourceService implements ManagedResourceService {

    private static final Logger log = LoggerFactory.getLogger(JPAManagedResourceService.class);

    private final Supplier<EntityManager> persistence;

    private final Validator validator;

    public JPAManagedResourceService(Supplier<EntityManager> persistence, ValidatorFactory validatorFactory) {
        this.persistence = persistence;
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public Optional<ManagedResourceRecord> getById(long id) {
        EntityManager jpa = persistence.get();

        ManagedResource managedResource = jpa.find(ManagedResource.class, id);

        return Optional.ofNullable(managedResource)
                .map(JPAManagedResourceService::entityToRecord);
    }

    @Override
    public ManagedResourceRecord save(SaveManagedResourceRequest managedResource) throws RManagerDataLayerException {
        validate(managedResource);

        EntityManager jpa = persistence.get();

        EntityTransaction transaction = jpa.getTransaction();
        transaction.begin();

        try {
            var entity = new ManagedResource();
            mergeEntityWithRecord(managedResource, entity);
            entity.setCapacity(managedResource.totalCapacity());
            jpa.persist(entity);
            transaction.commit();
            return entityToRecord(entity);
        } catch (RuntimeException e) {
            log.error("Data layer operation failed", e);
            transaction.rollback();
            throw new RManagerDataLayerException(e);
        }
    }

    @Override
    public void update(long id, SaveManagedResourceRequest managedResource) throws ManagedResourceNotFoundException, RManagerDataLayerException {
        validate(managedResource);

        EntityManager jpa = persistence.get();

        EntityTransaction transaction = jpa.getTransaction();
        transaction.begin();

        try {
            ManagedResource entity = jpa.find(ManagedResource.class, id);
            if (entity == null) {
                transaction.rollback();
                throw new ManagedResourceNotFoundException(id);
            }
            if (managedResource.totalCapacity() < entity.getCapacity()) {
                transaction.rollback();
                throw new IllegalArgumentException("Total capacity should be greater than claimed capacity!");
            }
            mergeEntityWithRecord(managedResource, entity);
            transaction.commit();
        } catch (RuntimeException e) {
            log.error("Data layer operation failed", e);
            transaction.rollback();
            throw new RManagerDataLayerException(e);
        }
    }

    private void validate(SaveManagedResourceRequest managedResource) {
        Set<ConstraintViolation<SaveManagedResourceRequest>> constraintViolations = validator.validate(managedResource);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public void delete(long id) throws ManagedResourceNotFoundException {
        EntityManager jpa = persistence.get();

        EntityTransaction transaction = jpa.getTransaction();
        transaction.begin();
        try {
            ManagedResource entity = jpa.find(ManagedResource.class, id);
            if (entity == null) {
                transaction.rollback();
                throw new ManagedResourceNotFoundException(id);
            }
            jpa.remove(entity);
        } catch (RuntimeException e) {
            log.error("Data layer operation failed", e);
            transaction.rollback();
            throw e;
        }
    }

    @Override
    public AllocationResultRecord allocate(long id, int units) throws ManagedResourceNotFoundException, RManagerDataLayerException {
        if (units == 0) {
            throw new IllegalArgumentException("Can't allocate zero units of a resource");
        }

        EntityManager jpa = persistence.get();

        EntityTransaction transaction = jpa.getTransaction();
        transaction.begin();
        try {
            ManagedResource entity = jpa.find(ManagedResource.class, id);
            if (entity == null) {
                transaction.rollback();
                throw new ManagedResourceNotFoundException(id);
            }

            int oldCapacity = entity.getCapacity();
            int newCapacity = oldCapacity - units;
            AllocationResult.Status status;
            String reason;
            if (newCapacity > entity.getTotalCapacity()) {
                status = AllocationResult.Status.REJECTED;
                reason = "Total capacity exceeded. Total = %s, got = %s".formatted(entity.getTotalCapacity(), newCapacity);
            } else if (newCapacity < 0) {
                status = AllocationResult.Status.REJECTED;
                reason = "Not enough capacity. Was available = %s, requested = %s".formatted(oldCapacity, units);
            } else {
                status = AllocationResult.Status.ACCEPTED;
                reason = null;
                entity.setCapacity(newCapacity);
            }
            var allocationResult = new AllocationResult(status, reason);
            var allocationRequest = new AllocationRequest();
            allocationRequest.setResult(allocationResult);
            allocationRequest.setResource(entity);
            allocationRequest.setPreviousResourceCapacity(oldCapacity);
            entity.getAllocationRequests().add(allocationRequest);

            jpa.persist(allocationRequest);

            transaction.commit();

            return new AllocationResultRecord(status, reason);
        } catch (RuntimeException e) {
            log.error("Data layer operation failed", e);
            throw new RManagerDataLayerException(e);
        }
    }

    private static void mergeEntityWithRecord(SaveManagedResourceRequest managedResource, ManagedResource entity) {
        entity.setName(managedResource.name());
        entity.setDescription(managedResource.description());
        entity.setTotalCapacity(managedResource.totalCapacity());
    }

    private static ManagedResourceRecord entityToRecord(ManagedResource entity) {
        return new ManagedResourceRecord(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCapacity(),
                entity.getTotalCapacity()
        );
    }
}