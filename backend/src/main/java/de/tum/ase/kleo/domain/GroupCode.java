package de.tum.ase.kleo.domain;

import org.codehaus.jackson.annotate.JsonCreator;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
public class GroupCode {

    private static final int PREFIX_CHARS_LEN = 3;
    private static final int POSTFIX_CHARS_LEN = 5;

    private static final String CODE_FORMAT = "%s-%s";

    @Column(nullable = false)
    private final String code;

    @JsonCreator
    private GroupCode(String code) {
        this.code = code;
    }

    public static GroupCode fromGroupName(String groupName) {
        return new GroupCode(format(CODE_FORMAT, createPrefix(groupName), randomPostfix()));
    }

    private static String createPrefix(String text) {
        final String alphanumericText = text.trim()
                .toUpperCase().replaceAll("/[^A-Za-z0-9 ]/", "");
        final String[] alphanumericTextTokens = alphanumericText.split("\\s+");

        final StringBuffer prefixBuilder = new StringBuffer();
        if (alphanumericTextTokens.length < PREFIX_CHARS_LEN) {
            final String extractedPrefix = alphanumericText
                    .substring(0, max(PREFIX_CHARS_LEN, alphanumericText.length()));

            prefixBuilder.append(extractedPrefix);

            // Add random letter if the prefix is still smaller than required
            if (extractedPrefix.length() < PREFIX_CHARS_LEN) {
                prefixBuilder.append(randomAlphabetic(PREFIX_CHARS_LEN - extractedPrefix.length()));
            }
        } else {
            stream(alphanumericTextTokens)
                    .forEach(token -> prefixBuilder.append(token.charAt(0)));
        }

        return prefixBuilder.toString();
    }

    private static String randomPostfix() {
        return randomNumeric(POSTFIX_CHARS_LEN);
    }

    @Override
    public String toString() {
        return code;
    }
}
