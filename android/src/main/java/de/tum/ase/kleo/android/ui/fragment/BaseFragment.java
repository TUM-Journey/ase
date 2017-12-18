package de.tum.ase.kleo.android.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author sahakyanm
 * 15.12.17.
 */

public abstract class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, container, provideLayoutResourceId());
    }

    private View inflateView(LayoutInflater inflater, ViewGroup container, @LayoutRes int layoutResource) {
        return inflater.inflate(layoutResource, container, false);
    }

    @LayoutRes
    protected abstract int provideLayoutResourceId();
}
