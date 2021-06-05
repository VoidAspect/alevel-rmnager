package com.alevel.rmanager.data.model.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alloc_requests")
public class AllocationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alloc_request_id_generator")
    @SequenceGenerator(name = "alloc_request_id_generator", sequenceName = "alloc_request_id_seq", allocationSize = 10)
    private Long id;

    @Access(AccessType.PROPERTY)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ManagedResource resource;

    @Column(name = "previous_resource_capacity", nullable = false)
    private int previousResourceCapacity;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Embedded
    private AllocationResult result;

    @PrePersist
    public void onCreate() {
        if (issuedAt == null) {
            issuedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ManagedResource getResource() {
        return resource;
    }

    public void setResource(ManagedResource resource) {
        this.resource = resource;
    }

    public int getPreviousResourceCapacity() {
        return previousResourceCapacity;
    }

    public void setPreviousResourceCapacity(int previousResourceCapacity) {
        this.previousResourceCapacity = previousResourceCapacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public AllocationResult getResult() {
        return result;
    }

    public void setResult(AllocationResult result) {
        this.result = result;
    }
}
