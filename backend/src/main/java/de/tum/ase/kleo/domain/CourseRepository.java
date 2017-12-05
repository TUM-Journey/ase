package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.CourseId;
import de.tum.ase.kleo.domain.id.GroupId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends CrudRepository<Course, CourseId> {

    List<Course> findAllByGroupIdsContaining(GroupId groupId);

    List<Course> findAllByGroupIdsIn(List<GroupId> groupIds);
}
