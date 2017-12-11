package de.tum.ase.kleo.android.client;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.tum.ase.kleo.android.client.invoker.ApiClient;
import de.tum.ase.kleo.android.client.invoker.auth.OAuth;
import de.tum.ase.kleo.android.client.invoker.auth.OAuthFlow;
import okhttp3.OkHttpClient;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.oltu.oauth2.client.request.OAuthClientRequest.tokenLocation;

public class BackendClient {

    private static final String OAUTH_TOKEN_ENDPOINT = "oauth/token";
    private static final Duration DEFAULT_OAUTH_TIMEOUT = Duration.ofSeconds(15);

    private final ApiClient apiClient;
    private final String basePath;
    private final String clientId;
    private final String secret;

    private final AtomicBoolean isAuthenciated = new AtomicBoolean();
    private final Map<String, Object> services = new ConcurrentHashMap<>();

    public BackendClient(String basePath, String clientId, String secret) {
        this.basePath = notBlank(basePath);
        this.clientId = notBlank(clientId);
        this.secret = secret;

        this.apiClient = new ApiClient();
        apiClient.setAdapterBuilder(apiClient.getAdapterBuilder().baseUrl(basePath));
    }

    public BackendClient(String basePath, String clientId) {
        this(basePath, clientId, null);
    }

    /**
     * Returns an implementation of the API endpoints of the backend
     *
     * @param apiClass a class of an API interface you want to acquire
     */
    @SuppressWarnings("unchecked")
    public <S> S as(Class<S> apiClass) {
        final String apiClassName = apiClass.getCanonicalName();

        final Object cachedApiService = services.get(apiClassName);
        synchronized (this) {
            if (cachedApiService != null) {
                return (S) cachedApiService;
            } else {
                S service = apiClient.createService(apiClass);
                services.put(apiClassName, service);
                return service;
            }
        }
    }

    public synchronized void authenticate(String username, String password, Duration timeout) {
        if (isAuthenciated.get())
            throw new IllegalStateException("Backend client has been already synchronized");

        try {
            final OAuth oAuth = new OAuth(okHttpClient(timeout), tokenLocation(basePath + OAUTH_TOKEN_ENDPOINT));
            oAuth.setFlow(OAuthFlow.password);
            oAuth.getTokenRequestBuilder()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);
            if (secret != null) oAuth.getTokenRequestBuilder().setClientSecret(secret);

            oAuth.updateAccessToken(null);

            apiClient.addAuthorization(oAuth.getClass().getName(), oAuth);
            isAuthenciated.set(true);
        } catch (IOException e) {
            throw new AuthenticationException("Failed to log in with given credentials", e);
        }
    }

    public void authenticate(String username, String password) {
        authenticate(username, password, null);
    }

    private static OkHttpClient okHttpClient(Duration timeout) {
        long millis = timeout != null ? timeout.toMillis() : DEFAULT_OAUTH_TIMEOUT.toMillis();
        return new OkHttpClient().newBuilder()
                .readTimeout(millis, TimeUnit.MILLISECONDS)
                .build();
    }
}
