package de.tum.ase.kleo.android.client;

import java.io.IOException;

import de.tum.ase.kleo.android.client.invoker.ApiClient;
import de.tum.ase.kleo.android.client.invoker.auth.OAuth;
import de.tum.ase.kleo.android.client.invoker.auth.OAuthFlow;

public class BackendClient extends ApiClient {

    private static final String OAUTH_TOKEN_ENDPOINT = "oauth/token";

    public BackendClient(String basePath, String clientId, String secret,
                         String username, String password) {
        super();
        super.setAdapterBuilder(getAdapterBuilder().baseUrl(basePath));

        final OAuth oAuth = new OAuth(OAuthFlow.password, "", basePath + OAUTH_TOKEN_ENDPOINT, null);
        super.addAuthorization(oAuth.getClass().getName(), oAuth);

        super.setCredentials(username,  password);
        super.getTokenEndPoint().setClientId(clientId).setUsername(username).setPassword(password);
        if (secret != null) super.getTokenEndPoint().setClientSecret(secret);

        try {
            oAuth.updateAccessToken(null);
        } catch (IOException e) {
            throw new AuthenticationException("Failed to log in with given credentials", e);
        }
    }

    public BackendClient(String basePath, String clientId,
                         String username, String password) {
        this(basePath, clientId, null, username, password);
    }
}
