package de.tum.ase.kleo.app.group.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Optional;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.Advertisement;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.AdvertisementBroadcaster;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.BackendHandshakeSupplier;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeServer;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.util.stream.Collectors.toList;

public class GroupAttendanceVerifierFragment extends ReactiveLayoutFragment {

    private GroupsApi groupsApi;

    public GroupAttendanceVerifierFragment() {
        super(R.layout.fragment_group_attendance_verifier);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {

    }
}
