package com.alevel.rmanager.data.service.jpa;

import com.alevel.rmanager.data.exception.ManagedResourceNotFoundException;
import com.alevel.rmanager.data.model.dto.AllocationResultRecord;
import com.alevel.rmanager.data.model.dto.ManagedResourceRecord;
import com.alevel.rmanager.data.model.dto.SaveManagedResourceRequest;
import com.alevel.rmanager.data.model.entity.AllocationResult;
import com.alevel.rmanager.data.model.entity.ManagedResource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JPAManagedResourceServiceTest extends JPATest {

    private JPAManagedResourceService subject;

    @BeforeEach
    void setUp() {
        subject = new JPAManagedResourceService(() -> session, validatorFactory);
    }

    @Test
    @DisplayName("when resource is created - should return record with id")
    void testCreateResource() {
        var saveResourceRequest = new SaveManagedResourceRequest(
                "testCreateResource",
                "test resource 1",
                100
        );

        ManagedResourceRecord record = assertDoesNotThrow(() -> subject.save(saveResourceRequest));

        assertAll(
                "persisted record should match input",
                () -> assertNotNull(record.id()),
                () -> assertEquals(saveResourceRequest.name(), record.name()),
                () -> assertEquals(saveResourceRequest.description(), record.description()),
                () -> assertEquals(saveResourceRequest.totalCapacity(), record.capacity()),
                () -> assertEquals(saveResourceRequest.totalCapacity(), record.totalCapacity())
        );
    }

    @Test
    @DisplayName("when save resource request is invalid - should throw exception")
    void testCreateResourceValidation() {
        assertThrows(ConstraintViolationException.class, () -> subject.save(new SaveManagedResourceRequest(
                null,
                "test resource 1",
                0)));
        assertThrows(ConstraintViolationException.class, () -> subject.save(new SaveManagedResourceRequest(
                "",
                "test resource 1",
                1)));
        assertThrows(ConstraintViolationException.class, () -> subject.save(new SaveManagedResourceRequest(
                "  ",
                "test resource 1",
                2)));
        assertThrows(ConstraintViolationException.class, () -> subject.save(new SaveManagedResourceRequest(
                "test 1",
                "test resource 1",
                -1)));


        assertThrows(ConstraintViolationException.class, () -> subject.update(1, new SaveManagedResourceRequest(
                null,
                "test resource 1",
                0)));
        assertThrows(ConstraintViolationException.class, () -> subject.update(1, new SaveManagedResourceRequest(
                "",
                "test resource 1",
                1)));
        assertThrows(ConstraintViolationException.class, () -> subject.update(1, new SaveManagedResourceRequest(
                "  ",
                "test resource 1",
                2)));
        assertThrows(ConstraintViolationException.class, () -> subject.update(1, new SaveManagedResourceRequest(
                "test 1",
                "test resource 1",
                -1)));
    }

    @Test
    @DisplayName("when resource is present - should retrieve it by id")
    void testGetResource() {
        Optional<ManagedResourceRecord> notPresent = subject.getById(-1);
        assertEquals(Optional.empty(), notPresent);

        ManagedResourceRecord record = assertDoesNotThrow(() -> subject.save(new SaveManagedResourceRequest(
                "testGetResource",
                null,
                10)));

        Optional<ManagedResourceRecord> present = subject.getById(record.id());
        assertTrue(present.isPresent());
        assertEquals(record, present.get());
    }

    @Test
    @DisplayName("when resource is present - should update it by id")
    void testUpdateResource() {
        SaveManagedResourceRequest update = new SaveManagedResourceRequest(
                "testUpdateResource - updated",
                "testUpdateResource - update description",
                12
        );
        assertThrows(ManagedResourceNotFoundException.class, () -> subject.update(-1, update));

        ManagedResourceRecord record = assertDoesNotThrow(() -> subject.save(new SaveManagedResourceRequest(
                "testUpdateResource",
                null,
                10)));

        assertDoesNotThrow(() -> subject.update(record.id(), update));

        Optional<ManagedResourceRecord> updated = subject.getById(record.id());
        assertTrue(updated.isPresent());
        assertEquals(
                new ManagedResourceRecord(
                        record.id(),
                        update.name(),
                        update.description(),
                        10,
                        update.totalCapacity()),
                updated.get());
    }

    @Test
    @DisplayName("when resource is present - should be able to delete it by id")
    void testDeleteResource() {
        assertThrows(ManagedResourceNotFoundException.class, () -> subject.delete(-1));

        long id = assertDoesNotThrow(() -> subject.save(new SaveManagedResourceRequest(
                "testDelete",
                null,
                10))).id();

        assertDoesNotThrow(() -> subject.allocate(id, 5));

        assertTrue(session.createQuery("select ar from AllocationRequest ar where ar.resource.id = :id")
                .setParameter("id", id)
                .getResultList().stream()
                .findAny().isPresent());

        assertDoesNotThrow(() -> subject.delete(id));

        Optional<ManagedResourceRecord> byId = subject.getById(id);

        assertEquals(Optional.empty(), byId);
        assertNull(session.find(ManagedResource.class, id));
        assertTrue(session.createQuery("select ar from AllocationRequest ar where ar.resource.id = :id")
                .setParameter("id", id)
                .getResultList().stream()
                .findAny().isEmpty());
    }

    @Test
    @DisplayName("when allocation requested - should allocate if capacity is valid")
    void testAllocate() {
        assertThrows(ManagedResourceNotFoundException.class, () -> subject.allocate(-1, 3));

        long id = assertDoesNotThrow(() -> subject.save(new SaveManagedResourceRequest(
                "testAllocate",
                null,
                10))).id();

        assertThrows(IllegalArgumentException.class, () -> subject.allocate(id, 0));

        AllocationResultRecord result1 = assertDoesNotThrow(() -> subject.allocate(id, 4));
        assertEquals(AllocationResult.Status.ACCEPTED, result1.status());

        Optional<ManagedResourceRecord> resource = subject.getById(id);
        assertTrue(resource.isPresent());
        assertEquals(6, resource.get().capacity());

        AllocationResultRecord result2 = assertDoesNotThrow(() -> subject.allocate(id, 6));
        assertEquals(AllocationResult.Status.ACCEPTED, result2.status());

        resource = subject.getById(id);
        assertTrue(resource.isPresent());
        assertEquals(0, resource.get().capacity());

        AllocationResultRecord result3 = assertDoesNotThrow(() -> subject.allocate(id, 1));
        assertEquals(AllocationResult.Status.REJECTED, result3.status());
        assertNotNull(result3.reason());

        resource = subject.getById(id);
        assertTrue(resource.isPresent());
        assertEquals(0, resource.get().capacity());

        AllocationResultRecord result4 = assertDoesNotThrow(() -> subject.allocate(id, -10));
        assertEquals(AllocationResult.Status.ACCEPTED, result4.status());

        resource = subject.getById(id);
        assertTrue(resource.isPresent());
        assertEquals(10, resource.get().capacity());

        AllocationResultRecord result5 = assertDoesNotThrow(() -> subject.allocate(id, -1));
        assertEquals(AllocationResult.Status.REJECTED, result5.status());
        assertNotNull(result5.reason());

        resource = subject.getById(id);
        assertTrue(resource.isPresent());
        assertEquals(10, resource.get().capacity());
    }
}