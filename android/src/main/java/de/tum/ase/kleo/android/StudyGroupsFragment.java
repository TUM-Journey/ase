package de.tum.ase.kleo.android;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.client.GroupsApi;
import de.tum.ase.kleo.android.client.dto.GroupDTO;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class StudyGroupsFragment extends Fragment {

    private GroupsApi groupsApi;

    private ProgressBar progressBar;
    private AlphaAnimation progressBarFadeInAnimation;
    private AlphaAnimation progressBarFadeOutAnimation;

    private RecyclerView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);

        progressBarFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        progressBarFadeOutAnimation.setDuration(500);
        progressBarFadeOutAnimation.setFillAfter(true);

        progressBarFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        progressBarFadeInAnimation.setDuration(500);
        progressBarFadeInAnimation.setFillAfter(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_study_groups, container, false);
        progressBar = view.findViewById(R.id.studyGroupsProgressBar);
        listView = view.findViewById(R.id.studyGroupsList);
        final LinearLayoutManager studentGroupsListLayoutManager = new LinearLayoutManager(view.getContext());
        listView.setLayoutManager(studentGroupsListLayoutManager);

        groupsApi.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> showLoadingProgressBar())
                .doFinally(this::hideLoadingProgressBar)
                .subscribe(this::populateStudentGroupsListView, this::showError);

        return view;
    }

    private void populateStudentGroupsListView(List<GroupDTO> groups) {
        final RecyclerView.Adapter<?> studentGroupsAdapter = new StudentGroupsAdapter(groups);
        listView.setAdapter(studentGroupsAdapter);
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showLoadingProgressBar() {
        progressBar.startAnimation(progressBarFadeInAnimation);
        progressBarFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void hideLoadingProgressBar() {
        progressBar.startAnimation(progressBarFadeOutAnimation);
        progressBarFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}
