package de.tum.ase.kleo.application.auth;

import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${security.realm}")
    private String realm;

    @Value("${security.provider.superuser.username}")
    private String superuserUsername;
    @Value("${security.provider.superuser.password}")
    private String superuserPassword;
    @Value("${security.provider.superuser.roles}")
    private UserRole[] superuserRoles;

    @Value("${security.provider.tum.roles}")
    private UserRole[] tumUserRoles;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .httpBasic()
                .realmName(realm)
                    .and()
                .csrf()
                .disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth
                .authenticationProvider(superuserAuthenticationProvider())
                .authenticationProvider(tumAuthenticationProvider());
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // provides the default AuthenticationManager as a Bean
        return super.authenticationManagerBean();
    }

    private AuthenticationProvider superuserAuthenticationProvider() {
        return new UsernamePasswordAuthenticationProvider(superuserUsername, superuserPassword, superuserRoles);
    }

    private AuthenticationProvider tumAuthenticationProvider() {
        return new TumAuthenticationProvider(userRepository, passwordEncoder(), tumUserRoles);
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
