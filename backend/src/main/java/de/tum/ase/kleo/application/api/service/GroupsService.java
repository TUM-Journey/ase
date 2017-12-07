package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.GroupsApiDelegate;
import de.tum.ase.kleo.application.api.dto.GroupDTO;
import de.tum.ase.kleo.application.api.dto.PassDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GroupsService implements GroupsApiDelegate {

    @Override
    public ResponseEntity<GroupDTO> addGroup(GroupDTO group) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addGroupStudent(String groupId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addGroupTutor(String groupId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteGroup(String groupId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteGroupStudent(String groupId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteGroupTutor(String groupId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupStudents(String groupId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getGroupTutors(String groupId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getGroups() {
        return null;
    }

    @Override
    public ResponseEntity<GroupDTO> updateGroup(String groupId, GroupDTO group) {
        return null;
    }

    @Override
    public ResponseEntity<PassDTO> generateSessionPass(String groupId, PassDTO pass) {
        return null;
    }

    @Override
    public ResponseEntity<Void> utilizeSessionPass(String groupId, String passCode) {
        return null;
    }
}
