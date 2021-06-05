package com.alevel.rmanager.data.model.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AllocationResult {

    public enum Status {
        ACCEPTED, REJECTED
    }

    @Column(nullable = false)
    private Status status;

    private String reason;

    public AllocationResult(Status status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public AllocationResult() {
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
