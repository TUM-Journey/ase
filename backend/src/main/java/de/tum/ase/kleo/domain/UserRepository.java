package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, UserId> {

    Optional<User> findOptionalByEmail(String email);
}
