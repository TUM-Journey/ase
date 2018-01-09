package de.tum.ase.kleo.android.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import de.tum.ase.kleo.android.KleoApplication;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.client.GroupsApi;
import de.tum.ase.kleo.android.client.dto.GroupDTO;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.android.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.android.ui.ProgressBars.fadeOut;

public class StudyGroupsFragment extends ReactiveLayoutFragment {

    private GroupsApi groupsApi;

    private ProgressBar progressBar;

    private RecyclerView listView;

    public StudyGroupsFragment() {
        super(R.layout.fragment_study_groups);
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
        final RecyclerView.Adapter<?> studentGroupsAdapter = new StudentGroupsAdapter(groups);
        listView.setAdapter(studentGroupsAdapter);
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
