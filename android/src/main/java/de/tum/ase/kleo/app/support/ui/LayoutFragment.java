package de.tum.ase.kleo.app.support.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class LayoutFragment extends Fragment {

    private final @LayoutRes int layoutResource;

    public LayoutFragment(@LayoutRes int layoutResource) {
        this.layoutResource = layoutResource;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(layoutResource, container, false);
        onCreateLayout(view, savedInstanceState);
        return view;
    }

    protected void onCreateLayout(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onFragmentCreated(getView(), savedInstanceState);
    }

    protected void onFragmentCreated(View view, Bundle state) {
    }
}
