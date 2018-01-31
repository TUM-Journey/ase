package eu.socialedge.edu.kleo.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.socialedge.edu.kleo.BuildConfig;
import eu.socialedge.edu.kleo.client.BackendClient;
import eu.socialedge.edu.kleo.di.MainSubComponent;

@Module(subcomponents = MainSubComponent.class)
public class BackendClientModule {

    @Provides
    @Singleton
    public BackendClient backendClient() {
        return new BackendClient(BuildConfig.BACKEND_BASE_URL,
                BuildConfig.BACKEND_CLIENT_ID, BuildConfig.BACKEND_CLIENT_SECRET);
    }
}
