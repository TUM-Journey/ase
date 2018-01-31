package eu.socialedge.edu.kleo.di;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import eu.socialedge.edu.kleo.MainActivity;

@Subcomponent
public interface MainSubComponent extends AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MainActivity> {}
}
