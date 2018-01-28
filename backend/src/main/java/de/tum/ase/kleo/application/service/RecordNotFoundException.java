package de.tum.ase.kleo.application.service;

public class RecordNotFoundException extends RuntimeException {

    private Class<?> recordType;

    public RecordNotFoundException(String s, Class<?> recordType) {
        super(s);
        this.recordType = recordType;
    }

    public RecordNotFoundException(String s, Class<?> recordType, Throwable throwable) {
        super(s, throwable);
        this.recordType = recordType;
    }

    public Class<?> recordType() {
        return recordType;
    }
}
