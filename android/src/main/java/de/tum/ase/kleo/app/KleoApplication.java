package de.tum.ase.kleo.app;

import android.app.Application;

import de.tum.ase.kleo.android.BuildConfig;
import de.tum.ase.kleo.app.client.BackendClient;

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
