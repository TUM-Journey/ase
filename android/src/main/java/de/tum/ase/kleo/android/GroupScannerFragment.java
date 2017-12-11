package de.tum.ase.kleo.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class GroupScannerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_group_scanner, container, false);

        final Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.breath);
        view.findViewById(R.id.radarIcon).startAnimation(shake);

        return view;
    }
}
