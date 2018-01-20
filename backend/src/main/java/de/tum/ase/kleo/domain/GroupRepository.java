package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.GroupId;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<Group, GroupId> {

    Group findOneByCode(GroupCode groupCode);

    List<Group> findAllByStudentIdsContaining(UserId userId);

    List<Group> findAllByAttendancesStudentId(UserId userId);

    default Group fetchGroupByIdOrCode(String groupIdOrCode) {
        val groupId = GroupId.of(groupIdOrCode);

        val groupById = findOne(groupId);
        if (groupById != null)
            return groupById;

        val groupCode = GroupCode.fromString(groupIdOrCode);

        val groupByCode = findOneByCode(groupCode);
        if (groupByCode != null)
            return groupByCode;

        return null;
    }
}
