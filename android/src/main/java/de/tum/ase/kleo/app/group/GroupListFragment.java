package de.tum.ase.kleo.app.group;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.Principal;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.support.ui.ResourceListLayoutFragment;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class GroupListFragment extends ResourceListLayoutFragment<GroupDTO> {

    private static final String GOTO_GROUP_DETAILS_BACK_STACK = "group_details";

    public GroupListFragment() {
        super(R.layout.fragment_group_list,
                R.id.group_list_view,
                R.layout.fragment_group_list_item,
                R.id.group_list_progressbar,
                R.id.group_list_no_records);
    }

    private void registerStudent(String groupId) {
        final Disposable regStudentReq = backendClient.as(GroupsApi.class)
                .addGroupStudent(groupId, backendClient.principal().id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .doOnError(this::showErrorMessage)
                .subscribe();

        disposeOnDestroy(regStudentReq);
    }

    private void deregisterStudent(String groupId) {
        final Disposable deregStudentReq = backendClient.as(GroupsApi.class)
                .deleteGroupStudent(groupId, backendClient.principal().id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .doOnError(this::showErrorMessage)
                .subscribe();

        disposeOnDestroy(deregStudentReq);
    }

    private Observable<GroupDTO> createNewGroup(String newGroupName) {
        return backendClient.as(GroupsApi.class)
                .addGroup(new GroupDTO().name(newGroupName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .doOnError(this::showErrorMessage);
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        final FloatingActionButton createNewBtn = view.findViewById(R.id.group_list_new_record);

        createNewBtn.setOnClickListener(l ->
                askForNewGroupName()
                        .subscribe(newGroupName ->
                                createNewGroup(newGroupName)
                                        .subscribe(this::appendResource)));
    }

    private Maybe<String> askForNewGroupName() {
        return Maybe.create(emitter -> {
            final EditText alertLocationInput = new EditText(getContext());
            alertLocationInput.setHint(R.string.group_list_new_record_name_hint);

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.group_list_new_record_popup_title)
                    .setView(alertLocationInput)
                    .setPositiveButton(R.string.create, (dialog, which) -> {
                        final String newGroupName = alertLocationInput.getText().toString();

                        if (isBlank(newGroupName)) {
                            emitter.onComplete();
                        } else {
                            emitter.onSuccess(newGroupName);
                        }

                        dialog.dismiss();
                    })
                    .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setOnDismissListener(dialog -> emitter.onComplete())
                    .show();
        });
    }

    @Override
    protected Observable<List<GroupDTO>> fetchResources() {
        return backendClient.as(GroupsApi.class).getGroups();
    }

    @Override
    protected void populateListItem(View view, GroupDTO group) {
        TextView name = view.findViewById(R.id.group_list_item_name_txt);
        TextView studentsCount = view.findViewById(R.id.group_list_item_student_count_txt);
        Switch registerSwitch = view.findViewById(R.id.group_list_item_registration_switch);

        final Principal currentUser = backendClient.principal();
        final List<String> registeredStudents = defaultIfNull(group.getStudentIds(), emptyList());

        name.setText(group.getName());
        studentsCount.setText(String.valueOf(registeredStudents.size()));
        if (currentUser.isStudent()) registerSwitch.setVisibility(View.VISIBLE);
        registerSwitch.setChecked(registeredStudents.contains(currentUser.id()));

        if (currentUser.isStudent()) {
            registerSwitch.setOnClickListener(v -> {
                final Integer oldStudentsCount = Integer.valueOf(studentsCount.getText().toString());

                if (registerSwitch.isChecked()) {
                    registerStudent(group.getId());
                    studentsCount.setText(String.valueOf(oldStudentsCount + 1));
                } else {
                    deregisterStudent(group.getId());
                    studentsCount.setText(String.valueOf(oldStudentsCount - 1));
                }
            });
        }

        view.setOnClickListener(v -> openGroupDetailsFragment(group));
    }

    private void openGroupDetailsFragment(GroupDTO group) {
        final GroupDetailsFragment groupDetailsFragment = new GroupDetailsFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(GroupDetailsFragment.ARG_BUNDLE_GROUP, group);
        groupDetailsFragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.main_container, groupDetailsFragment)
                .addToBackStack(GOTO_GROUP_DETAILS_BACK_STACK)
                .commit();
    }
}
