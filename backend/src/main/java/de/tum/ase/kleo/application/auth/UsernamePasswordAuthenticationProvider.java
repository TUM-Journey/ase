package de.tum.ase.kleo.application.auth;

import de.tum.ase.kleo.domain.UserRole;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final String username;
    private final String password;
    private final List<GrantedAuthority> grantedAuthorities;

    public UsernamePasswordAuthenticationProvider(String username, String password, UserRole... userRoles) {
        this.username = username;
        this.password = password;
        this.grantedAuthorities = stream(userRoles)
                .map(UserRole::name)
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!username.equals(authentication.getName()))
            return null;
        if (!password.equals(authentication.getCredentials().toString()))
            return null;

        val superuserPrincipal = new UserPrincipal(UUID.randomUUID().toString(), username, username);
        return new UsernamePasswordAuthenticationToken(superuserPrincipal, null, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
