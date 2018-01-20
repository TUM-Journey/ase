package de.tum.ase.kleo.app.group.advertisement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import de.tum.ase.kleo.android.BuildConfig;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;

public class GroupAdvertisementScannerFragment extends ReactiveLayoutFragment {

    private GroupAdvertisementScanner adScanner;

    public GroupAdvertisementScannerFragment() {
        super(R.layout.fragment_group_advertisement_scanner);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adScanner = new GroupAdvertisementScanner(BuildConfig.BLUETOOTH_PARCEL_ID);
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.breath);
        view.findViewById(R.id.radarIcon).startAnimation(shake);

        // TODO: imp
        adScanner.scan().subscribe(device -> {
            Toast.makeText(getContext(), "Device with this MAC is broadcasting: "
                            + device.getAddress(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        adScanner.stopScanning();
    }
}
