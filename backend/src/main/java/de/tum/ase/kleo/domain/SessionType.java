package de.tum.ase.kleo.domain;

import lombok.val;

public enum SessionType {
    EXERCISE, TUTORIAL, SEMINAR, OTHER;

    public static SessionType from(Enum<?> enm) {
        if (enm == null)
            return null;

        return from(enm.name());
    }

    public static SessionType from(String str) {
        val sessionTypes = values();

        for (val sessionType : sessionTypes) {
            if (sessionType.name().equalsIgnoreCase(str))
                return sessionType;
        }

        return null;
    }
}
