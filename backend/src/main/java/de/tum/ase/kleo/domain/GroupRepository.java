package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.UserId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, GroupId> {

    List<Group> findAllByStudentIdsContaining(UserId userId);

    List<Group> findAllByTutorIdsContaining(UserId userId);
}
