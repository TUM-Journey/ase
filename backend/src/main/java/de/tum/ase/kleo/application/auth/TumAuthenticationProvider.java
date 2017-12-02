package de.tum.ase.kleo.application.auth;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.UserRole;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class TumAuthenticationProvider implements AuthenticationProvider {

    private static final String SHIBBOLETH_LOGIN_PAGE = "https://www.moodle.tum.de/Shibboleth.sso/Login" +
            "?providerId=https%3A%2F%2Ftumidp.lrz.de%2Fidp%2Fshibboleth" +
            "&target=https%3A%2F%2Fwww.moodle.tum.de%2Fauth%2Fshibboleth%2Findex.php";

    private static final String SHIBBOLETH_USERNAME_INPUT_NAME = "j_username";
    private static final String SHIBBOLETH_PASSWORD_INPUT_NAME = "j_password";
    private static final String SHIBBOLETH_SUBMIT_BTN_NAME = "_eventId_proceed";
    private static final String SHIBBOLETH_ERROR_XPATH = "//p[contains(@class, \"form-error\")]";

    private static final String MOODLE_USERID_XPATH = "//*[@data-userid]/@data-userid";
    private static final String MOODLE_EDIT_PROFILE_URL = "https://www.moodle.tum.de/user/edit.php?id=%s";
    private static final String MOODLE_EDIT_PROFILE_FNAME_INPUT_NAME = "firstname";
    private static final String MOODLE_EDIT_PROFILE_SNAME_INPUT_NAME = "lastname";
    private static final String MOODLE_EDIT_PROFILE_MATRIK_INPUT_NAME = "idnumber";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TumAuthenticationProvider(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        val email = authentication.getName();
        val password = authentication.getCredentials().toString();

        val userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            val user = userOpt.get();
            if (!passwordEncoder.matches(password, user.passwordHash()))
                throw new BadCredentialsException("Password is invalid");

            return new UsernamePasswordAuthenticationToken(user, null,
                    userGrantedAuthorities(user.userRoles()));
        } else {
            val shibbolethUser = fetchShibbolethUser(email, password);
            userRepository.save(shibbolethUser);

            return new UsernamePasswordAuthenticationToken(shibbolethUser, null,
                    userGrantedAuthorities(shibbolethUser.userRoles()));
        }
    }

    private User fetchShibbolethUser(String email, String password) {
        try (val webClient = new WebClient()) {
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);

            val loginPage = (HtmlPage) webClient.getPage(SHIBBOLETH_LOGIN_PAGE);

            val usernameInput = (HtmlInput) loginPage.getElementByName(SHIBBOLETH_USERNAME_INPUT_NAME);
            val passwordInput = (HtmlInput) loginPage.getElementByName(SHIBBOLETH_PASSWORD_INPUT_NAME);
            val submitBtn = (HtmlButton) loginPage.getElementByName(SHIBBOLETH_SUBMIT_BTN_NAME);

            usernameInput.setValueAttribute(email);
            passwordInput.setValueAttribute(password);

            val moodlePage = (HtmlPage) submitBtn.click();

            if (moodlePage.getFirstByXPath(SHIBBOLETH_ERROR_XPATH) != null)
                throw new AuthenticationServiceException("Failed to fetch Shibboleth user " +
                        "(username and/or password are invalid)");

            val userId = ((DomAttr) moodlePage.getFirstByXPath(MOODLE_USERID_XPATH)).getValue();

            val moodleEditProfilePage = (HtmlPage) webClient.getPage(format(MOODLE_EDIT_PROFILE_URL, userId));
            val fnameInput = (HtmlInput) moodleEditProfilePage.getElementByName(MOODLE_EDIT_PROFILE_FNAME_INPUT_NAME);
            val snameInput = (HtmlInput) moodleEditProfilePage.getElementByName(MOODLE_EDIT_PROFILE_SNAME_INPUT_NAME);
            val matricInput = (HtmlInput) moodleEditProfilePage.getElementByName(MOODLE_EDIT_PROFILE_MATRIK_INPUT_NAME);

            val name = fnameInput.getValueAttribute() + " " + snameInput.getValueAttribute();
            val studentId = matricInput.getValueAttribute();
            val passwordHash = passwordEncoder.encode(password);

            return new User(userId, email, passwordHash, name, studentId);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to navigate through Shibboleth auth page", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private List<GrantedAuthority> userGrantedAuthorities(List<UserRole> userRoles) {
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(toList());
    }
}
