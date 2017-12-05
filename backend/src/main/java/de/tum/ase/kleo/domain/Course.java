package de.tum.ase.kleo.domain;

import de.tum.ase.kleo.domain.id.CourseId;
import de.tum.ase.kleo.domain.id.GroupId;
import eu.socialedge.ddd.domain.AggregateRoot;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@Entity @Access(AccessType.FIELD)
@Accessors(fluent = true) @ToString
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class Course extends AggregateRoot<CourseId> {

    @Getter
    @Column(nullable = false)
    private String name;

    @Column
    @Getter @Setter
    private String description;

    @ElementCollection
    @CollectionTable(name = "course_groups", joinColumns = @JoinColumn(name = "course_id"))
    private final Set<GroupId> groupIds = new HashSet<>();

    public Course(CourseId id, String name, String description, Set<GroupId> groupIds) {
        super(nonNull(id) ? id : new CourseId());
        this.name = notBlank(name);
        this.description = description;

        if (groupIds != null)
            this.groupIds.addAll(groupIds);
    }

    public Course(String name, String description) {
        this(null, name, description, null);
    }

    public Course(String name) {
        this(name, null);
    }

    public void name(String name) {
        this.name = notBlank(name);
    }

    public Set<GroupId> groupIds() {
        return unmodifiableSet(groupIds);
    }

    public void addGroup(GroupId groupId) {
        groupIds.add(notNull(groupId));
    }

    public boolean hasGroup(GroupId groupId) {
        return groupIds.contains(groupId);
    }

    public boolean removeGroup(GroupId groupId) {
        return groupIds.remove(groupId);
    }
}
