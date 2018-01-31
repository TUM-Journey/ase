package eu.socialedge.edu.kleo.di;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import eu.socialedge.edu.kleo.App;

@Component(modules = {
        AndroidInjectionModule.class,
        BackendClientModule.class,
        BuildersModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(App app);

        AppComponent build();
    }

    void inject(App app);
}
