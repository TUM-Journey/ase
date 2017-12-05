package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.SessionId;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends CrudRepository<Session, SessionId> {

    List<Session> findAllByAttendancesUserId(UserId userId);
}
