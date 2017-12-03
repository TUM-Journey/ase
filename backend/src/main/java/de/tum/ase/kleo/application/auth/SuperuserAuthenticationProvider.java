package de.tum.ase.kleo.application.auth;

import de.tum.ase.kleo.domain.UserRole;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static java.util.Collections.singletonList;

public class SuperuserAuthenticationProvider implements AuthenticationProvider {

    private final String username;
    private final String password;

    public SuperuserAuthenticationProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!username.equals(authentication.getName()))
            return null;
        if (!password.equals(authentication.getCredentials().toString()))
            return null;

        val superuserPrincipal = new UserPrincipal(UUID.randomUUID().toString(), username, username);
        return new UsernamePasswordAuthenticationToken(superuserPrincipal, null,
                singletonList(new SimpleGrantedAuthority(UserRole.SUPERUSER.name())));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
