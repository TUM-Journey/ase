package de.tum.ase.kleo.android.util;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Author sahakyanm
 * 15.12.17.
 */

public final class Validator {

    private Validator() {
    }

    private static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PATTERN_PASSWORD = "^[a-zA-Z0-9_]{4,}$"; // Minimum 4 alphanumeric characters;

    /**
     * Validates password with the given regex pattern.
     *
     * @param password The password we want to validate
     * @param emptyMsg The error msg when username is empty
     * @param errorMsg The error msg when it doesn't match to the pattern
     * @return an error msg or null
     */
    public static String validatePassword(String password, String emptyMsg, String errorMsg) {
        return validate(password, PATTERN_PASSWORD, emptyMsg, errorMsg);
    }

    /**
     * Validates email with the given regex pattern.
     *
     * @param email    The email we need to validate
     * @param emptyMsg The error msg when email is empty
     * @param errorMsg The error msg when it doesn't match to the pattern
     * @return an error msg or null
     */
    public static String validateEmail(String email, String emptyMsg, String errorMsg) {
        return validate(email, PATTERN_EMAIL, emptyMsg, errorMsg);
    }

    private static String validate(String value, String validationPattern, String emptyMsg, String errorMsg) {
        String output = null;
        if (TextUtils.isEmpty(value)) {
            output = emptyMsg;
        } else {
            Pattern pattern = Pattern.compile(validationPattern);
            if (!pattern.matcher(value).matches()) {
                output = errorMsg;
            }
        }
        return output;
    }
}
