package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.CoursesApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService implements CoursesApiDelegate {

    @Override
    public ResponseEntity<CourseDTO> addCourse(CourseDTO course) {
        return null;
    }

    @Override
    public ResponseEntity<SessionDTO> addCourseSession(String courseId, SessionDTO session) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addCourseTutor(String courseId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCourse(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCourseSession(String courseId, String sessionId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCourseTutor(String courseId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<CourseDTO> getCourse(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<SessionDTO> getCourseSession(String courseId, String sessionId) {
        return null;
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getCourseSessionAttendances(String courseId, String sessionId) {
        return null;
    }

    @Override
    public ResponseEntity<List<SessionDTO>> getCourseSessions(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseTutors(String courseId) {
        return null;
    }

    @Override
    public ResponseEntity<List<CourseDTO>> getCourses() {
        return null;
    }

    @Override
    public ResponseEntity<PassDTO> signupForCourseSession(String courseId, String sessionId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> unsignupForCourseSession(String courseId, String sessionId, String userId) {
        return null;
    }

    @Override
    public ResponseEntity<CourseDTO> updateCourse(String courseId, CourseDTO course) {
        return null;
    }

    @Override
    public ResponseEntity<SessionDTO> updateCourseSession(String courseId, String sessionId, SessionDTO session) {
        return null;
    }
}
