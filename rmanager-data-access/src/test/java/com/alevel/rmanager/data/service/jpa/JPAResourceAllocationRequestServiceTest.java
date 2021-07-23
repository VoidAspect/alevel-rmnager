package com.alevel.rmanager.data.service.jpa;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.model.dto.AllocationRequestRecord;
import com.alevel.rmanager.data.model.dto.SaveManagedResourceRequest;
import com.alevel.rmanager.data.model.entity.AllocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JPAResourceAllocationRequestServiceTest extends JPATest {

    private JPAResourceAllocationRequestService subject;

    private JPAManagedResourceService resourceService;

    @BeforeEach
    void setUp() {
        subject = new JPAResourceAllocationRequestService(() -> session);
        resourceService = new JPAManagedResourceService(() -> session, validatorFactory);
    }

    @Test
    @DisplayName("when allocation request is present - should retrieve it by id")
    void testGetAllocationRequestById() {

        assertEquals(Optional.empty(), subject.getById(-1));

        Long resourceId = assertDoesNotThrow(() -> resourceService.save(new SaveManagedResourceRequest(
                "testGetAllocationRequestById",
                null,
                10
        ))).id();

        var momentBeforeAllocation = Instant.now();

        assertDoesNotThrow(() -> resourceService.allocate(resourceId, 3));

        Long requestId = session.createQuery("select ar.id from AllocationRequest ar where ar.resource.id = :id", Long.class)
                .setParameter("id", resourceId)
                .getSingleResult();

        Optional<AllocationRequestRecord> result = subject.getById(requestId);
        assertTrue(result.isPresent());

        var allocationRequest = result.get();

        assertEquals(requestId, allocationRequest.id());
        assertEquals(resourceId, allocationRequest.resourceId());
        assertEquals(10, allocationRequest.previousResourceCapacity());
        assertEquals(3, allocationRequest.capacity());
        assertFalse(momentBeforeAllocation.isAfter(allocationRequest.issuedAt()));
        assertSame(AllocationResult.Status.ACCEPTED, allocationRequest.result().status());
        assertNull(allocationRequest.result().reason());
    }

    @Test
    @DisplayName("when resource is present - should retrieve its alloc requests by resource id")
    void testGetAllocationRequestsByResourceId() {

        assertThrows(ManagedResourceNotFoundException.class, () -> subject.getByResourceId(-1));

        Long resourceId = assertDoesNotThrow(() -> resourceService.save(new SaveManagedResourceRequest(
                "testGetAllocationRequestsByResourceId",
                null,
                5
        ))).id();

        List<AllocationRequestRecord> result1 = assertDoesNotThrow(() -> subject.getByResourceId(resourceId));
        assertEquals(List.of(), result1);

        assertDoesNotThrow(() -> resourceService.allocate(resourceId, 3));
        assertDoesNotThrow(() -> resourceService.allocate(resourceId, 4));

        List<AllocationRequestRecord> result2 = assertDoesNotThrow(() -> subject.getByResourceId(resourceId));

        assertEquals(2, result2.size());

        assertNotNull(result2.get(0).id());
        assertEquals(4, result2.get(0).capacity());
        assertEquals(2, result2.get(0).previousResourceCapacity());
        assertEquals(resourceId, result2.get(0).resourceId());
        assertSame(AllocationResult.Status.REJECTED, result2.get(0).result().status());
        assertNotNull(result2.get(0).result().reason());

        assertNotNull(result2.get(1).id());
        assertEquals(3, result2.get(1).capacity());
        assertEquals(5, result2.get(1).previousResourceCapacity());
        assertEquals(resourceId, result2.get(1).resourceId());
        assertSame(AllocationResult.Status.ACCEPTED, result2.get(1).result().status());
        assertNull(result2.get(1).result().reason());

        assertFalse(result2.get(0).issuedAt().isBefore(result2.get(1).issuedAt()));
    }
}