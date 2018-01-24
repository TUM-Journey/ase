package de.tum.ase.kleo.app.group.attendance.advertisement;

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
import de.tum.ase.kleo.app.client.dto.PassDTO;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.Advertisement;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.AdvertisementBroadcaster;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.BackendHandshakeSupplier;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeServer;
import de.tum.ase.kleo.app.support.ui.ArrayAdapterItem;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.util.stream.Collectors.toList;

public class GroupAdvertisementBroadcasterFragment extends ReactiveLayoutFragment {

    private GroupsApi groupsApi;
    private Spinner spinner;
    private AdvertisementBroadcaster adBroadcaster;
    private HandshakeServer handshakeServer;

    public GroupAdvertisementBroadcasterFragment() {
        super(R.layout.fragment_group_advertisement_broadcaster);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);
        adBroadcaster = AdvertisementBroadcaster.createDefault(HandshakeServer.SERVICE_UUID);
        handshakeServer = HandshakeServer.create(getContext(), new BackendHandshakeSupplier(groupsApi));
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        spinner = view.findViewById(R.id.groupAdBroadcasterChooser);
        populateGroupChooser();

        final ToggleButton broadcastToggle = view.findViewById(R.id.groupAdBroadcasterSwitchBtn);
        broadcastToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                final Optional<String> chosenGroupCode = getChosenGroupCode();
                if (!chosenGroupCode.isPresent()) {
                    Toast.makeText(getContext(), R.string.group_ad_select_first, Toast.LENGTH_LONG).show();
                    return;
                }

                final String groupCode = chosenGroupCode.get();
                adBroadcaster.broadcast(Advertisement.from(groupCode));
                handshakeServer.listen().subscribe(this::disableGroupChooser);
            } else {
                adBroadcaster.stop();
                handshakeServer.stop();
                enableGroupChooser();
            }
        });
    }

    private void populateGroupChooser() {
        final Disposable groupsReq = groupsApi.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groups -> {
                    final List<ArrayAdapterItem<String>> groupChooserItems = groups.stream()
                            .map(group -> ArrayAdapterItem.of(group.getName(), group.getCode()))
                            .collect(toList());

                    final ArrayAdapter<ArrayAdapterItem<String>> adapter
                            = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                                groupChooserItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(adapter);
                }, this::showError);

        disposeOnDestroy(groupsReq);
    }

    @SuppressWarnings("unchecked")
    private Optional<String> getChosenGroupCode() {
        final Object selectedItem = spinner.getSelectedItem();

        if (selectedItem == null)
            return Optional.empty();

        return Optional.of(((ArrayAdapterItem<String>) selectedItem).value());
    }

    private void enableGroupChooser() {
        spinner.setEnabled(true);
    }

    private void disableGroupChooser() {
        spinner.setEnabled(false);
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
