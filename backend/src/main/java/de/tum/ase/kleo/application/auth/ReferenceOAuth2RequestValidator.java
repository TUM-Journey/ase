package de.tum.ase.kleo.application.auth;

import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenRequest;

import java.util.Set;

/**
 * {@code ReferenceOAuth2RequestValidator} validates OAuth2 requests to the
 * {@code AuthorizationEndpoint} and {@code TokenEndpoint}. In contrast to
 * {@code DefaultOAuth2RequestValidator}, this implementation allows empty scopes
 * as defined in RFC6749.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-3.3">
 *     OAuth 2.0 - 3.3. Access Token Scope</a>
 */
public class ReferenceOAuth2RequestValidator implements OAuth2RequestValidator {

    public void validateScope(AuthorizationRequest authorizationRequest, ClientDetails client) throws InvalidScopeException {
        validateScope(authorizationRequest.getScope(), client.getScope());
    }

    public void validateScope(TokenRequest tokenRequest, ClientDetails client) throws InvalidScopeException {
        validateScope(tokenRequest.getScope(), client.getScope());
    }

    private void validateScope(Set<String> requestScopes, Set<String> clientScopes) {
        if (clientScopes != null && !clientScopes.isEmpty()) {
            for (String scope : requestScopes) {
                if (!clientScopes.contains(scope)) {
                    throw new InvalidScopeException("Invalid scope: " + scope, clientScopes);
                }
            }
        }
    }
}
