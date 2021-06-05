package com.alevel.rmanager.data.service.jpa;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.model.dto.AllocationRequestRecord;
import com.alevel.rmanager.data.model.dto.AllocationResultRecord;
import com.alevel.rmanager.data.model.entity.AllocationRequest;
import com.alevel.rmanager.data.service.ResourceAllocationRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class JPAResourceAllocationRequestService implements ResourceAllocationRequestService {

    private static final Logger log = LoggerFactory.getLogger(JPAResourceAllocationRequestService.class);

    private final Supplier<EntityManager> persistence;

    public JPAResourceAllocationRequestService(Supplier<EntityManager> persistence) {
        this.persistence = persistence;
    }

    @Override
    public List<AllocationRequestRecord> getByResourceId(long resourceId) throws ManagedResourceNotFoundException {
        EntityManager jpa = persistence.get();

        EntityTransaction transaction = jpa.getTransaction();
        transaction.begin();

        try {
            TypedQuery<Boolean> checkIfExists = jpa.createQuery("""
                    select (count(r) > 0) as exists
                    from ManagedResource r where r.id = :id
                    """, Boolean.class);

            checkIfExists.setParameter("id", resourceId);
            if (!checkIfExists.getSingleResult()) {
                transaction.rollback();
                throw new ManagedResourceNotFoundException(resourceId);
            }

            TypedQuery<AllocationRequestRecord> findByResourceId = jpa.createQuery("""
                    select new com.alevel.rmanager.data.model.dto.AllocationRequestRecord(
                        ar.id,
                        ar.resource.id,
                        ar.capacity,
                        ar.previousResourceCapacity,
                        ar.issuedAt,
                        ar.result.status,
                        ar.result.reason
                    ) from AllocationRequest ar where ar.resource.id = :id
                    """, AllocationRequestRecord.class);

            findByResourceId.setParameter("id", resourceId);

            transaction.commit();
            return findByResourceId.getResultList();
        } catch (RuntimeException e) {
            log.atError().setCause(e).log("Data layer operation failed");
            transaction.rollback();
            throw e;
        }
    }

    @Override
    public Optional<AllocationRequestRecord> getById(long id) {
        EntityManager jpa = persistence.get();

        AllocationRequest allocationRequest = jpa.find(AllocationRequest.class, id);

        return Optional.ofNullable(allocationRequest)
                .map(entity -> new AllocationRequestRecord(
                        entity.getId(),
                        entity.getResource().getId(),
                        entity.getCapacity(),
                        entity.getPreviousResourceCapacity(),
                        entity.getIssuedAt(),
                        new AllocationResultRecord(
                                entity.getResult().getStatus(),
                                entity.getResult().getReason()
                        )
                ));
    }
}
