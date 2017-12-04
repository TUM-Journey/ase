package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.CoursesApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.application.api.mapping.*;
import de.tum.ase.kleo.domain.CourseRepository;
import de.tum.ase.kleo.domain.Pass;
import de.tum.ase.kleo.domain.UserRepository;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

@Service
@Transactional(readOnly = true)
public class CourseService implements CoursesApiDelegate {

    private final CourseRepository courseRepository;
    private final CourseToDtoMapper courseToDtoMapper;
    private final CourseFromDtoMapper courseFromDtoMapper;
    private final CourseDtoMerger courseDtoMerger;

    private final SessionToDtoMapper sessionToDtoMapper;
    private final SessionFromDtoMapper sessionFromDtoMapper;
    private final SessionAttendanceDtoMapper sessionAttendanceDtoMapper;
    private final SessionDtoMerger sessionDtoMerger;

    private final UserRepository userRepository;
    private final UserToDtoMapper userToDtoMapper;

    public CourseService(CourseRepository courseRepository, CourseToDtoMapper courseToDtoMapper,
                         CourseFromDtoMapper courseFromDtoMapper, CourseDtoMerger courseDtoMerger,
                         SessionToDtoMapper sessionToDtoMapper, SessionFromDtoMapper sessionFromDtoMapper,
                         SessionAttendanceDtoMapper sessionAttendanceDtoMapper, SessionDtoMerger sessionDtoMerger,
                         UserRepository userRepository, UserToDtoMapper userToDtoMapper) {
        this.courseRepository = courseRepository;
        this.courseToDtoMapper = courseToDtoMapper;
        this.courseFromDtoMapper = courseFromDtoMapper;
        this.courseDtoMerger = courseDtoMerger;
        this.sessionToDtoMapper = sessionToDtoMapper;
        this.sessionFromDtoMapper = sessionFromDtoMapper;
        this.sessionAttendanceDtoMapper = sessionAttendanceDtoMapper;
        this.sessionDtoMerger = sessionDtoMerger;
        this.userRepository = userRepository;
        this.userToDtoMapper = userToDtoMapper;
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<CourseDTO> addCourse(CourseDTO courseDto) {
        val course = courseFromDtoMapper.map(courseDto);
        courseRepository.save(course);

        return ResponseEntity.ok(courseToDtoMapper.map(course));
    }

    @Override
    @Transactional
    public ResponseEntity<SessionDTO> addCourseSession(String courseId, SessionDTO sessionDto) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        val session = sessionFromDtoMapper.map(sessionDto);
        course.addSession(session);

        return ResponseEntity.ok(sessionToDtoMapper.map(session));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> addCourseTutor(String courseId, String userId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        course.addTutor(user);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteCourse(String courseId) {
        if (!courseRepository.exists(courseId))
            return ResponseEntity.notFound().build();

        courseRepository.delete(courseId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteCourseSession(String courseId, String sessionId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!course.removeSession(sessionId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteCourseTutor(String courseId, String userId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!course.removeTutor(userId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<CourseDTO> getCourse(String courseId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(courseToDtoMapper.map(course));
    }

    @Override
    public ResponseEntity<SessionDTO> getCourseSession(String courseId, String sessionId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        return course.session(sessionId)
                .map(session -> ResponseEntity.ok(sessionToDtoMapper.map(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<AttendanceDTO>> getCourseSessionAttendances(String courseId, String sessionId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val session = course.session(sessionId).orElse(null);
        if (session == null)
            return ResponseEntity.notFound().build();

        val usersWithPassesToGivenSession = userRepository.findAllByPassesSession(session);

        val attendancesDtosPerUser = sessionAttendanceDtoMapper.map(usersWithPassesToGivenSession);
        return attendancesDtosPerUser.stream().flatMap(Collection::stream)
                .collect(collectingAndThen(Collectors.toList(), ResponseEntity::ok));
    }

    @Override
    public ResponseEntity<List<SessionDTO>> getCourseSessions(String courseId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(sessionToDtoMapper.map(course.sessions()));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseTutors(String courseId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userToDtoMapper.map(course.tutors()));
    }

    @Override
    public ResponseEntity<List<CourseDTO>> getCourses() {
        val courses = courseRepository.findAll();
        return ResponseEntity.ok(courseToDtoMapper.map(courses));
    }

    @Override
    @Transactional
    public ResponseEntity<PassDTO> signupForCourseSession(String courseId, String sessionId, String userId) {
        val user = userRepository.findOne(userId);
        if (user == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val session = course.session(sessionId).orElse(null);
        if (session == null)
            return ResponseEntity.notFound().build();

        val sessionPass = new Pass(session);
        user.addPass(sessionPass);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<Void> unsignupForCourseSession(String courseId, String sessionId, String userId) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val session = course.session(sessionId).orElse(null);
        if (session == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val user = userRepository.findByIdAndPassesSession(userId, session).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();

        user.pass(session).ifPresent(user::removePass);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<CourseDTO> updateCourse(String courseId, CourseDTO courseDto) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        courseDtoMerger.merge(courseDto, course);

        return ResponseEntity.ok(courseToDtoMapper.map(course));
    }

    @Override
    @Transactional
    public ResponseEntity<SessionDTO> updateCourseSession(String courseId, String sessionId, SessionDTO sessionDto) {
        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        val session = course.session(sessionId).orElse(null);
        if (session == null)
            return ResponseEntity.notFound().build();

        sessionDtoMerger.merge(sessionDto, session);

        return ResponseEntity.ok(sessionToDtoMapper.map(session));
    }
}
