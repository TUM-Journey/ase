package de.tum.ase.kleo.application.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class TestProtectedService {

    @PreAuthorize("hasAuthority('STUDENT')")
    public String helloStudent() {
        return "Hello protected world, Student!";
    }

    @PreAuthorize("hasAuthority('SUPERUSER')")
    public String helloSuperuser() {
        return "Hello protected world, Superuser!";
    }
}
