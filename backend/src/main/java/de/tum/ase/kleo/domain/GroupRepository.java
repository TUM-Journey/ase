package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.GroupId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends CrudRepository<Group, GroupId> {
}
