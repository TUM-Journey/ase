package de.tum.ase.kleo.android;

import android.app.Application;

import de.tum.ase.kleo.android.client.BackendClient;

public class KleoApplication extends Application {

    private BackendClient backendClient;

    @Override
    public void onCreate() {
        super.onCreate();

        configureBackendClient();
    }

    private void configureBackendClient() {
        final String baseUrl = getString(R.string.backend_baseUrl);
        final String clientId = getString(R.string.backend_clientId);
        final String clientSecret = getString(R.string.backend_clientSecret);

        backendClient = new BackendClient(baseUrl, clientId, clientSecret);
    }

    public BackendClient backendClient() {
        return backendClient;
    }
}
