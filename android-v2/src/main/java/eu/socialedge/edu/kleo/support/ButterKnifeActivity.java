package eu.socialedge.edu.kleo.support;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.socialedge.edu.kleo.R;

public abstract class ButterKnifeActivity extends Activity {

    private @LayoutRes final int layoutRes;
    private Unbinder unbinder;

    protected ButterKnifeActivity(@LayoutRes int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
