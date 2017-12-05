package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.CoursesApiDelegate;
import de.tum.ase.kleo.application.api.dto.CourseDTO;
import de.tum.ase.kleo.application.api.dto.GroupDTO;
import de.tum.ase.kleo.application.api.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CoursesService implements CoursesApiDelegate {

    @Override
    public ResponseEntity<CourseDTO> addCourse(CourseDTO course) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addCourseGroup(String courseId, String groupId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCourse(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCourseGroup(String courseId, String groupId) {
        return null;
    }

    @Override
    public ResponseEntity<CourseDTO> getCourse(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getCourseGroups(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseStudents(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseTutors(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<CourseDTO>> getCourses(String groupIds) {
        return null;
    }

    @Override
    public ResponseEntity<CourseDTO> updateCourse(String courseId, CourseDTO course) {
        return null;
    }
}
