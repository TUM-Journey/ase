package de.tum.ase.kleo.android.client;

import de.tum.ase.kleo.android.client.invoker.ApiClient;
import de.tum.ase.kleo.android.client.invoker.auth.OAuth;
import de.tum.ase.kleo.android.client.invoker.auth.OAuthFlow;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class BackendClient extends ApiClient {

    private static final String OAUTH_TOKEN_ENDPOINT = "oauth/token";

    public BackendClient(String basePath, String clientId, String secret, String username, String password) {
        super();
        super.setAdapterBuilder(getAdapterBuilder().baseUrl(basePath));

        final OAuth oAuth = new OAuth(OAuthFlow.password, "", basePath + OAUTH_TOKEN_ENDPOINT, null);
        super.addAuthorization("OAuth2Password", oAuth);

        super.setCredentials(username,  password);
        super.getTokenEndPoint().setClientId(clientId).setUsername(username).setPassword(password);
        if (secret != null) super.getTokenEndPoint().setClientSecret(secret);
    }

    public BackendClient(String basePath, String clientId, String username, String password) {
        this(basePath, clientId, null, username, password);
    }
}
