package de.tum.ase.kleo.application.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.val;

public class UserPrincipalAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private final static String PRINCIPAL_ID = "user_id";
    private final static String PRINCIPAL_EMAIL = "user_email";
    private final static String PRINCIPAL_NAME = "user_name";
    private final static String PRINCIPAL_STUDENT_ID = "user_student_id";

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        val response = new LinkedHashMap<String, Object>();
        val principal = (UserPrincipal) authentication.getPrincipal();

        val principalId = principal.getId();
        val principalEmail = principal.getEmail();
        val principalName = principal.getName();
        val principalStudentId = principal.getStudentId();

        response.put(PRINCIPAL_ID, principalId);
        response.put(PRINCIPAL_EMAIL, principalEmail);
        response.put(PRINCIPAL_NAME, principalName);

        if (principalStudentId != null) {
            response.put(PRINCIPAL_STUDENT_ID, principalStudentId);
        }

        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> data) {
        val principalId = (String) data.get(PRINCIPAL_ID);
        val principalEmail = (String) data.get(PRINCIPAL_EMAIL);
        val principalName = (String) data.get(PRINCIPAL_NAME);
        val principalStudentId = (String) data.get(PRINCIPAL_STUDENT_ID);

        val principal = new UserPrincipal(principalId, principalEmail, principalName, principalStudentId);
        val authorities = getAuthorities(data);

        return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return Collections.emptyList();
        }

        val authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
