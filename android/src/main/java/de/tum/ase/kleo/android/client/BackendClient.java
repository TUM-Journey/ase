package de.tum.ase.kleo.android.client;

import com.auth0.android.jwt.JWT;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
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

    private final AtomicBoolean isAuthenticated = new AtomicBoolean();
    private OAuth oAuth;

    private final static String PRINCIPAL_ID = "user_id";
    private final static String PRINCIPAL_EMAIL = "user_email";
    private final static String PRINCIPAL_NAME = "user_name";
    private final static String PRINCIPAL_STUDENT_ID = "user_student_id";
    private final static String PRINCIPAL_AUTHORITIES = "authorities";

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

    private synchronized Principal authenticate(String username, String password, @SuppressWarnings("SameParameterValue") Duration timeout) {
        if (isAuthenticated.get())
            throw new IllegalStateException("Backend client has been already synchronized");

        try {
            oAuth = new OAuth(okHttpClient(timeout), tokenLocation(basePath + OAUTH_TOKEN_ENDPOINT));
            oAuth.setFlow(OAuthFlow.password);
            oAuth.getTokenRequestBuilder()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);
            if (secret != null) oAuth.getTokenRequestBuilder().setClientSecret(secret);

            oAuth.updateAccessToken(null);

            apiClient.addAuthorization(oAuth.getClass().getName(), oAuth);
            isAuthenticated.set(true);

            return principal();
        } catch (IOException e) {
            throw new AuthenticationException("Failed to log in with given credentials", e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public Principal authenticate(String username, String password) {
        return authenticate(username, password, null);
    }

    public boolean isAuthenticated() {
        return isAuthenticated.get();
    }

    public Principal principal() {
        final JWT jwt = new JWT(oAuth.getAccessToken());

        final String principalId = jwt.getClaim(PRINCIPAL_ID).asString();
        final String principalEmail = jwt.getClaim(PRINCIPAL_EMAIL).asString();
        final String principalName = jwt.getClaim(PRINCIPAL_NAME).asString();
        final String principalStudentId = jwt.getClaim(PRINCIPAL_STUDENT_ID).asString();
        final List<String> principalAuthorities = jwt.getClaim(PRINCIPAL_AUTHORITIES).asList(String.class);

        return new Principal(principalId, principalEmail, principalName, principalStudentId,
                Principal.Authority.from(principalAuthorities));
    }

    private static OkHttpClient okHttpClient(Duration timeout) {
        long millis = timeout != null ? timeout.toMillis() : DEFAULT_OAUTH_TIMEOUT.toMillis();
        return new OkHttpClient().newBuilder()
                .readTimeout(millis, TimeUnit.MILLISECONDS)
                .build();
    }
}
