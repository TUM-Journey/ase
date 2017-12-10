package de.tum.ase.kleo.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Animation shake = AnimationUtils.loadAnimation(this, R.anim.breath);
        findViewById(R.id.radarIcon).startAnimation(shake);
    }
}
