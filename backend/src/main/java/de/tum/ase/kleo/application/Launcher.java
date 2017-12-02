package de.tum.ase.kleo.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "de.tum.ase.kleo")
@EnableJpaRepositories(basePackages = "de.tum.ase.kleo")
@EntityScan(basePackages = "de.tum.ase.kleo")
public class Launcher {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Launcher.class).run(args);
    }
}
