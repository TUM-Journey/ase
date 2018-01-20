package de.tum.ase.kleo.app.group;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.Principal;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeOut;

public class GroupListFragment extends ReactiveLayoutFragment {

    private Principal currentUser;

    private GroupsApi groupsApi;

    private ProgressBar progressBar;

    private RecyclerView listView;

    public GroupListFragment() {
        super(R.layout.fragment_group_list);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);
        currentUser = backendClient.principal().blockingGet();
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        progressBar = view.findViewById(R.id.studyGroupsProgressBar);
        listView = view.findViewById(R.id.studyGroupsList);
        final LinearLayoutManager studentGroupsListLayoutManager = new LinearLayoutManager(view.getContext());
        listView.setLayoutManager(studentGroupsListLayoutManager);

        final Disposable groupsReq = groupsApi.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> fadeIn(progressBar))
                .doFinally(() -> fadeOut(progressBar))
                .subscribe(this::populateStudentGroupsListView, this::showError);

        disposeOnDestroy(groupsReq);
    }

    private void populateStudentGroupsListView(List<GroupDTO> groups) {
        final String currentUserId = currentUser.id();

        if (currentUser.isStudent()) {
            final GroupListAdapter groupListAdapter
                    = GroupListAdapter.withRegisterSwitchEnabled(groups, currentUserId,
                        this::registerStudent, this::deregisterStudent);

            listView.setAdapter(groupListAdapter);
        } else {
            listView.setAdapter(GroupListAdapter.withRegisterSwitchDisabled(groups));
        }
    }

    private void registerStudent(String groupId) {
        final Disposable regStudentReq = groupsApi.addGroupStudent(groupId, currentUser.id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Toast.makeText(getContext(), getString(R.string.group_student_registered), Toast.LENGTH_LONG).show())
                .subscribe();

        disposeOnDestroy(regStudentReq);
    }

    private void deregisterStudent(String groupId) {
        final Disposable deregStudentReq = groupsApi.deleteGroupStudent(groupId, currentUser.id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Toast.makeText(getContext(), getString(R.string.group_student_deregistered), Toast.LENGTH_LONG).show())
                .subscribe();

        disposeOnDestroy(deregStudentReq);
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
