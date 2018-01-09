package de.tum.ase.kleo.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

abstract class LayoutFragment extends Fragment {

    private final @LayoutRes int layoutResource;

    LayoutFragment(@LayoutRes int layoutResource) {
        this.layoutResource = layoutResource;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(layoutResource, container, false);
        onCreateLayout(view, inflater, container, savedInstanceState);
        return view;
    }

    protected void onCreateLayout(View layoutView, LayoutInflater inflater, ViewGroup container,
                                  Bundle bundle) {
    }
}
