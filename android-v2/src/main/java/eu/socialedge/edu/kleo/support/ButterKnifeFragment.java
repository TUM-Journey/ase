package eu.socialedge.edu.kleo.support;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.val;

public abstract class ButterKnifeFragment extends Fragment {

    private @LayoutRes final int layoutRes;
    private Unbinder unbinder;

    protected ButterKnifeFragment(@LayoutRes int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        val view = inflater.inflate(layoutRes, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
