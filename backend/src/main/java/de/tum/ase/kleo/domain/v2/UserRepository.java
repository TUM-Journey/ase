package de.tum.ase.kleo.domain.v2;

import de.tum.ase.kleo.domain.v2.id.UserId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, UserId> {

    List<User> findAllByIdIn(List<UserId> userIds);

    Optional<User> findByEmail(String email);
}
