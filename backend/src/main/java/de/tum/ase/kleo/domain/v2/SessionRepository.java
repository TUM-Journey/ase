package de.tum.ase.kleo.domain.v2;

import de.tum.ase.kleo.domain.v2.id.SessionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<Session, SessionId> {
}
