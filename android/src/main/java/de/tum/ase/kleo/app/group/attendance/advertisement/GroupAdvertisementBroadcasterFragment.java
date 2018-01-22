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
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.Advertisement;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.AdvertisementBroadcaster;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeServer;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.util.stream.Collectors.toList;

public class GroupAdvertisementBroadcasterFragment extends ReactiveLayoutFragment {

    private GroupsApi groupsApi;
    private Spinner spinner;
    private AdvertisementBroadcaster adBroadcaster;

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
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
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
                disableGroupChooser();
            } else {
                adBroadcaster.stop();
                enableGroupChooser();
            }
        });
    }

    private void populateGroupChooser() {
        final Disposable groupsReq = groupsApi.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groups -> {
                    final List<GroupChooserItem> groupChooserItems = groups.stream()
                            .map(group -> new GroupChooserItem(group.getCode(), group.getName()))
                            .collect(toList());

                    final ArrayAdapter<GroupChooserItem> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, groupChooserItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(adapter);
                }, this::showError);

        disposeOnDestroy(groupsReq);
    }

    private Optional<String> getChosenGroupCode() {
        final Object selectedItem = spinner.getSelectedItem();

        if (selectedItem == null)
            return Optional.empty();

        return Optional.of(((GroupChooserItem) selectedItem).groupCode);
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

    private static class GroupChooserItem {
        private final String groupCode;
        private final String name;

        public GroupChooserItem(String groupCode, String name) {
            this.groupCode = groupCode;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
