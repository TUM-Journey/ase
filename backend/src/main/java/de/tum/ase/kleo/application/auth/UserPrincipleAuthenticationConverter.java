package de.tum.ase.kleo.application.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserPrincipleAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private final static String PRINCIPAL = "user";

    private final ObjectMapper objectMapper;

    public UserPrincipleAuthenticationConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserPrincipleAuthenticationConverter() {
        this(new ObjectMapper());
    }

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        val response = new LinkedHashMap<String, Object>();

        try {
            response.put(PRINCIPAL, objectMapper.writeValueAsString(authentication.getPrincipal()));
        } catch (JsonProcessingException e) {
            throw new AuthenticationServiceException("Failed to convert authentication principle", e);
        }

        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        val rawPrincipal = map.get(PRINCIPAL);
        if (rawPrincipal != null) {
            try {
                val principal = objectMapper.readValue((String) rawPrincipal, UserPrincipal.class);
                val authorities = getAuthorities(map);

                return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
            } catch (IOException e) {
                throw new AuthenticationServiceException("Failed to read authentication principle", e);
            }

        }
        return null;
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
