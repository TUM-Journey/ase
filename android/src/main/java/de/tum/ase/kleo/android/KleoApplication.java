package de.tum.ase.kleo.android;

import android.app.Application;

import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.util.NetworkUtils;

public class KleoApplication extends Application {

    private BackendClient backendClient;

    @Override
    public void onCreate() {
        super.onCreate();
        backendClient = buildBackendClient();
    }

    private BackendClient buildBackendClient() {
        final String baseUrl = NetworkUtils.Configuration.BASE_URL;
        final String clientId = NetworkUtils.Configuration.CLIENT_ID;
        final String clientSecret = NetworkUtils.Configuration.CLIENT_SECRET;

        return new BackendClient(baseUrl, clientId, clientSecret);
    }

    public BackendClient backendClient() {
        return backendClient;
    }
}
