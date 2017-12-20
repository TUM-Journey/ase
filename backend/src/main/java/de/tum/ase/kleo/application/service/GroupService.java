package de.tum.ase.kleo.application.service;

import de.tum.ase.kleo.domain.Attendance;
import de.tum.ase.kleo.domain.Group;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.id.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GroupService {
    
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Stream<Pair<Group, Set<Attendance>>> getUserGroupAttendances(UserId userId) {
        return groupRepository.findAllByAttendancesStudentId(userId).stream()
                .map(group -> Pair.of(group, group.attendances(userId)));
    }
    
    public Stream<Group> getUserGroups(UserId userId) {
        return groupRepository.findAllByStudentIdsContaining(userId).stream();
    }
}
