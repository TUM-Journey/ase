package de.tum.ase.kleo.app.support.ui;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;

public class ProgressBars {

    private static final long DEFAULT_ANIMATION_DURATION = 500;

    private static final Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f) {{
        setFillAfter(true);
    }};

    private static final Animation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f) {{
        setFillAfter(true);
    }};


    private ProgressBars() {
        throw new AssertionError("No de.tum.ase.kleo.app.support.ui.ProgressBars instance for you");
    }


    public static void fadeOut(ProgressBar progressBar, long duration) {
        fadeOutAnimation.setDuration(duration);

        progressBar.startAnimation(fadeOutAnimation);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public static void fadeOut(ProgressBar progressBar) {
        fadeOut(progressBar, DEFAULT_ANIMATION_DURATION);
    }

    public static void fadeIn(ProgressBar progressBar, long duration) {
        fadeInAnimation.setDuration(duration);

        progressBar.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public static void fadeIn(ProgressBar progressBar) {
        fadeIn(progressBar, DEFAULT_ANIMATION_DURATION);
    }
}
