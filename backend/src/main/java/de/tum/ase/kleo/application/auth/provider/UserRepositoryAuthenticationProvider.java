package de.tum.ase.kleo.application.auth.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.tum.ase.kleo.domain.UserRepository;
import lombok.val;

public class UserRepositoryAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryAuthenticationProvider(UserRepository userRepository,
                                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        val email = authentication.getName();
        val password = authentication.getCredentials().toString();

        return userRepository.findOptionalByEmail(email)
                .filter(usr -> passwordEncoder.matches(password, usr.passwordHash()))
                .map(usr -> new UsernamePasswordAuthenticationToken(usr, null,
                        UserGrantedAuthorities.fromUserRoles(usr.userRoles())))
                .orElse(null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
