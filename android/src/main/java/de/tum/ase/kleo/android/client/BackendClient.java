package de.tum.ase.kleo.android.client;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import de.tum.ase.kleo.android.client.invoker.ApiClient;
import de.tum.ase.kleo.android.client.invoker.auth.OAuth;
import de.tum.ase.kleo.android.client.invoker.auth.OAuthFlow;
import okhttp3.OkHttpClient;

import static org.apache.oltu.oauth2.client.request.OAuthClientRequest.tokenLocation;

class BackendClient extends ApiClient {

    private static final String OAUTH_TOKEN_ENDPOINT = "oauth/token";
    private static final Duration DEFAULT_OAUTH_TIMEOUT = Duration.ofSeconds(15);

    public BackendClient(String basePath, String clientId, String secret,
                         String username, String password, Duration authTimeout) {
        super();
        super.setAdapterBuilder(getAdapterBuilder().baseUrl(basePath));

        OAuth oAuth = new OAuth(okHttpClient(authTimeout), tokenLocation(basePath + OAUTH_TOKEN_ENDPOINT));
        oAuth.setFlow(OAuthFlow.password);
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

    public BackendClient(String basePath, String clientId, String username, String password) {
        this(basePath, clientId, null, username, password, null);
    }

    private static OkHttpClient okHttpClient(Duration timeout) {
        if (timeout == null)
            timeout = DEFAULT_OAUTH_TIMEOUT;

        return new OkHttpClient().newBuilder()
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }
}
