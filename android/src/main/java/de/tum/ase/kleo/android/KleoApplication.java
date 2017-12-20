package de.tum.ase.kleo.android;

import android.app.Application;

import de.tum.ase.kleo.android.client.BackendClient;

public class KleoApplication extends Application {

    private BackendClient backendClient;

    @Override
    public void onCreate() {
        super.onCreate();

        backendClient = buildBackendClient();
    }

    private BackendClient buildBackendClient() {
        return new BackendClient(BuildConfig.BACKEND_BASE_URL,
                BuildConfig.BACKEND_CLIENT_ID, BuildConfig.BACKEND_CLIENT_SECRET);
    }

    public BackendClient backendClient() {
        return backendClient;
    }
}
