package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.SessionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<Session, SessionId> {
}
