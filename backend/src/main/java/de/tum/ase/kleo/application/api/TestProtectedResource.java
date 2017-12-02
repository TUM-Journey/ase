package de.tum.ase.kleo.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestProtectedResource {

    @Autowired
    private TestProtectedService service;

    @GetMapping("/api/student")
    public String pr0tectedStudent() {
        return service.helloStudent();
    }

    @GetMapping("/api/superuser")
    public String pr0tectedSuperuser() {
        return service.helloSuperuser();
    }
}
