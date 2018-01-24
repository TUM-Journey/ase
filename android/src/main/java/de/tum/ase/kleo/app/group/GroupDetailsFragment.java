package de.tum.ase.kleo.app.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.support.ui.LayoutFragment;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupDetailsFragment extends ReactiveLayoutFragment {

    public static final String ARG_BUNDLE_GROUP = "group_details";

    private BackendClient backendClient;
    private GroupDTO group;

    public GroupDetailsFragment() {
        super(R.layout.fragment_group_details);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backendClient = ((KleoApplication) getActivity().getApplication()).backendClient();
    }

    @Override
    protected void onCreateLayout(View view, Bundle savedInstanceState) {
        final Serializable rawGroup = getArguments().getSerializable(ARG_BUNDLE_GROUP);

        if (rawGroup == null) {
            throw new IllegalStateException("GroupDetailsSessionListFragment requires group arg");
        } else if (!GroupDTO.class.equals(rawGroup.getClass())) {
            throw new IllegalStateException("GroupDetailsSessionListFragment 'group' arg is not " +
                    "of GroupDTO type");
        }

        group = (GroupDTO) rawGroup;
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        final EditText groupNameInput = view.findViewById(R.id.group_details_name_input);
        final Button groupRenameBtn = view.findViewById(R.id.group_details_rename_btn);

        groupNameInput.setText(group.getName());
        if (backendClient.principal().isTutor()) {
            groupRenameBtn.setOnClickListener(l -> renameGroup(groupNameInput.getText().toString()));
        } else {
            groupRenameBtn.setEnabled(false);
            groupNameInput.setEnabled(false);
        }
    }

    private void renameGroup(String newName) {
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
