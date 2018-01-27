package de.tum.ase.kleo.app.group.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.group.details.session.GroupDetailsSessionListFragment;
import de.tum.ase.kleo.app.support.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupDetailsFragment extends ReactiveLayoutFragment {

    public static final String ARG_BUNDLE_GROUP_ID = "group_details_id";

    private BackendClient backendClient;
    private String groupId;

    public GroupDetailsFragment() {
        super(R.layout.fragment_group_details);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = getArguments().getString(ARG_BUNDLE_GROUP_ID);
        if (groupId == null) {
            throw new IllegalStateException("GroupDetailsSessionListFragment requires group id arg");
        }

        backendClient = ((KleoApplication) getActivity().getApplication()).backendClient();
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        final EditText groupNameInput = view.findViewById(R.id.group_details_name_input);
        final Button groupRenameBtn = view.findViewById(R.id.group_details_rename_btn);

        backendClient.as(GroupsApi.class).getGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(group -> {
                    groupNameInput.setText(group.getName());

                    if (backendClient.principal().isTutor()) {
                        groupRenameBtn.setOnClickListener(l
                                -> renameGroup(group, groupNameInput.getText().toString()));
                    } else {
                        groupRenameBtn.setEnabled(false);
                        groupNameInput.setEnabled(false);
                    }

                    embedGroupSessionList();
                });
    }

    private void embedGroupSessionList() {
        final GroupDetailsSessionListFragment groupSessionListFragment
                = new GroupDetailsSessionListFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(GroupDetailsSessionListFragment.ARG_BUNDLE_GROUP_ID, groupId);
        groupSessionListFragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.group_details_session_frame, groupSessionListFragment)
                .commit();
    }

    private void renameGroup(GroupDTO group, String newName) {
        group.setName(newName);
        final Disposable disposable = backendClient.as(GroupsApi.class)
                .updateGroup(group.getId(), group)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() ->
                        Toast.makeText(getContext(),
                                R.string.group_details_renaming_confirmation_toast,
                                    Toast.LENGTH_LONG).show())
                .subscribe();

        disposeOnDestroy(disposable);
    }
}
