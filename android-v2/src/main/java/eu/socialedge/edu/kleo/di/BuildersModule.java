package eu.socialedge.edu.kleo.di;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import eu.socialedge.edu.kleo.MainActivity;

@Module
public abstract class BuildersModule {

    @Binds
    @IntoMap
    @ActivityKey(MainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
        bindMainActivityInjectorFactory(MainSubComponent.Builder builder);
}
