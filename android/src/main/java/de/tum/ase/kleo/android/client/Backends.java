package de.tum.ase.kleo.android.client;

import java.util.HashMap;
import java.util.Map;

public class Backends {

    private static final Backends thiz = new Backends();

    private String basePath;
    private String clientId;
    private String clientSecret;
    private BackendClient client;
    private final Map<String, Object> services = new HashMap<>();

    public static void init(String basePath, String clientId) {
        init(basePath, clientId, null);
    }

    public static void init(String basePath, String clientId, String clientSecret) {
        thiz.basePath = basePath;
        thiz.clientId = clientId;
        thiz.clientSecret = clientSecret;

    }

    public static void login(String username, String password) {
        if (thiz.basePath == null || thiz.clientId == null)
            throw new IllegalStateException("Initialize object first");
        
        thiz.client = new BackendClient(thiz.basePath, thiz.clientId, thiz.clientSecret,
                username, password);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> apiClass) {
        if (thiz.client == null)
            throw new IllegalStateException("Initialize object first");
        
        String apiClassName = apiClass.getCanonicalName();

        Object cachedApiService = thiz.services.get(apiClassName);
        if (cachedApiService != null) {
            return (T) cachedApiService;
        }

        T service = thiz.client.createService(apiClass);
        thiz.services.put(apiClassName, service);
        return service;
    }
}
