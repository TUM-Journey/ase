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

    default boolean existsByIdOrCode(String groupIdOrCode) {
        return findOneByIdOrCode(groupIdOrCode).isPresent();
    }

    default Optional<Group> findOneByIdOrCode(String groupIdOrCode) {
        val groupId = GroupId.of(groupIdOrCode);

        val groupById = findOne(groupId);
        if (groupById != null)
            return Optional.of(groupById);

        val groupCode = GroupCode.fromString(groupIdOrCode);

        val groupByCode = findOneByCode(groupCode);
        if (groupByCode != null)
            return Optional.of(groupByCode);

        return Optional.empty();
    }

    default boolean deleteByIdOrCode(String groupIdOrCode) {
        final Optional<Group> groupOpt = findOneByIdOrCode(groupIdOrCode);

        if (!groupOpt.isPresent())
            return false;

        groupOpt.ifPresent(group -> delete(group.id()));
        return true;
    }
}
