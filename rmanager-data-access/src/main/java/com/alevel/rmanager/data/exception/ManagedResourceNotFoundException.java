package com.alevel.rmanager.data.exception;

public class ManagedResourceNotFoundException extends RManagerDataAccessException {
    public ManagedResourceNotFoundException(Long id) {
        super("Managed resource with id = " + id + " was not found!");
    }
}
