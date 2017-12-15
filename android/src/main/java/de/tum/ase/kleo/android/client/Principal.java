package de.tum.ase.kleo.android.client;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Principal {

    enum Authority {
        SUPERUSER, TUTOR, USER;

        public static List<Authority> from(List<String> rawAuthorities) {
            return rawAuthorities.stream()
                    .map(ra -> Authority.valueOf(ra.toUpperCase()))
                    .collect(toList());
        }
    }

    private final String id;

    private final String email;

    private final String name;

    private final String studentId;

    private final List<Authority> authorities;

    public Principal(String id, String email, String name, String studentId, List<Authority> authorities) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.authorities = authorities;
    }

    public String id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String name() {
        return name;
    }

    public String studentId() {
        return studentId;
    }

    public List<Authority> authorities() {
        return Collections.unmodifiableList(authorities);
    }

    public boolean isSuperuser() {
        return authorities.contains(Authority.SUPERUSER);
    }

    public boolean isTutor() {
        return authorities.contains(Authority.TUTOR);
    }

    public boolean isUser() {
        return authorities.contains(Authority.USER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Principal principal = (Principal) o;

        if (!id.equals(principal.id)) return false;
        if (!email.equals(principal.email)) return false;
        if (!name.equals(principal.name)) return false;
        if (studentId != null ? !studentId.equals(principal.studentId) : principal.studentId != null)
            return false;
        return authorities != null ? authorities.equals(principal.authorities) : principal.authorities == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (studentId != null ? studentId.hashCode() : 0);
        result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Principal{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", authorities=" + authorities +
                '}';
    }
}
