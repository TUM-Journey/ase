package de.tum.ase.kleo.application.api.service;

import de.tum.ase.kleo.application.api.CoursesApiDelegate;
import de.tum.ase.kleo.application.api.dto.*;
import de.tum.ase.kleo.domain.Course;
import de.tum.ase.kleo.domain.CourseRepository;
import de.tum.ase.kleo.domain.GroupRepository;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.id.CourseId;
import de.tum.ase.kleo.domain.id.GroupId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.eclipse.jetty.util.StringUtil.isBlank;

@Service
@Transactional(readOnly = true)
public class CoursesService implements CoursesApiDelegate {
    
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private final CourseDtoMapper courseDtoMapper;
    private final CourseDtoMerger courseDtoMerger;
    private final GroupDtoMapper groupDtoMapper;
    private final UserDtoSerializer userDtoSerializer;

    @Autowired
    public CoursesService(CourseRepository courseRepository, GroupRepository groupRepository,
                          UserRepository userRepository, CourseDtoMapper courseDtoMapper,
                          CourseDtoMerger courseDtoMerger, GroupDtoMapper groupDtoMapper,
                          UserDtoSerializer userDtoSerializer) {
        this.courseRepository = courseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.courseDtoMapper = courseDtoMapper;
        this.courseDtoMerger = courseDtoMerger;
        this.groupDtoMapper = groupDtoMapper;
        this.userDtoSerializer = userDtoSerializer;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<CourseDTO> addCourse(CourseDTO dto) {
        val newCourse = new Course(dto.getName(), dto.getDescription());
        val savedCourse = courseRepository.save(newCourse);

        return ResponseEntity.ok(courseDtoMapper.toDto(savedCourse));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> addCourseGroup(String courseIdRaw, String groupIdRaw) {
        val courseId = CourseId.of(courseIdRaw);
        val groupId = GroupId.of(groupIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!groupRepository.exists(groupId))
            return ResponseEntity.notFound().build();

        course.addGroup(groupId);

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteCourse(String courseIdRaw) {
        val courseId = CourseId.of(courseIdRaw);

        if (!courseRepository.exists(courseId))
            return ResponseEntity.notFound().build();

        courseRepository.delete(courseId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<Void> deleteCourseGroup(String courseIdRaw, String groupIdRaw) {
        val courseId = CourseId.of(courseIdRaw);
        val groupId = GroupId.of(groupIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        // TODO: Add notFound message to distinct 404 resps (relies on issue #2)
        if (!course.removeGroup(groupId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<CourseDTO> getCourse(String courseIdRaw) {
        val courseId = CourseId.of(courseIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(courseDtoMapper.toDto(course));
    }

    @Override
    public ResponseEntity<List<GroupDTO>> getCourseGroups(String courseIdRaw) {
        val courseId = CourseId.of(courseIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        val courseGroups = groupRepository.findAll(course.groupIds());

        return ResponseEntity.ok(groupDtoMapper.toDto(courseGroups));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseStudents(String courseIdRaw) {
        val courseId = CourseId.of(courseIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        val students = stream(groupRepository.findAll(course.groupIds()).spliterator(), false)
                .flatMap(group -> stream(userRepository.findAll(group.studentIds()).spliterator(), false))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(userDtoSerializer.toDto(students));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getCourseTutors(String courseIdRaw) {
        val courseId = CourseId.of(courseIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        val tutors = stream(groupRepository.findAll(course.groupIds()).spliterator(), false)
                .flatMap(group -> stream(userRepository.findAll(group.tutorIds()).spliterator(), false))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(userDtoSerializer.toDto(tutors));
    }

    @Override
    public ResponseEntity<List<CourseDTO>> getCourses(String groupIdsRaw) {
        if (isBlank(groupIdsRaw))
            return ResponseEntity.ok(courseDtoMapper.toDto(courseRepository.findAll()));

        val groupIds = Arrays.stream(groupIdsRaw.split(","))
                .map(String::trim)
                .map(GroupId::of)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(courseDtoMapper.toDto(courseRepository.findAllByGroupIdsIn(groupIds)));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<CourseDTO> updateCourse(String courseIdRaw, CourseDTO dto) {
        val courseId = CourseId.of(courseIdRaw);

        val course = courseRepository.findOne(courseId);
        if (course == null)
            return ResponseEntity.notFound().build();

        courseDtoMerger.merge(dto, course);

        return ResponseEntity.ok(courseDtoMapper.toDto(course));
    }
}
