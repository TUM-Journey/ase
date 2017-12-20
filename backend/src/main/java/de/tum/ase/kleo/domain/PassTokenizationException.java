package de.tum.ase.kleo.domain;

public class PassTokenizationException extends RuntimeException {

    public PassTokenizationException(String s) {
        super(s);
    }

    public PassTokenizationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
