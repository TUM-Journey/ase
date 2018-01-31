package eu.socialedge.edu.kleo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import eu.socialedge.edu.kleo.client.BackendClient;
import eu.socialedge.edu.kleo.support.ButterKnifeActivity;

public class MainActivity extends ButterKnifeActivity {

    @Inject
    BackendClient backendClient;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        Log.d("ADSASD", "INJECTED = " + backendClient.toString());
    }
}
