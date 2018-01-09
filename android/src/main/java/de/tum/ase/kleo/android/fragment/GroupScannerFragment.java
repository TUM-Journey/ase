package de.tum.ase.kleo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import de.tum.ase.kleo.android.R;

public class GroupScannerFragment extends LayoutFragment {

    public GroupScannerFragment() {
        super(R.layout.fragment_group_scanner);
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.breath);
        view.findViewById(R.id.radarIcon).startAnimation(shake);
    }

}
