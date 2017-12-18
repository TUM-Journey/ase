package de.tum.ase.kleo.android.studying.groups;

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

import java.io.IOException;
import java.util.List;

import de.tum.ase.kleo.android.KleoApplication;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.client.GroupsApi;
import de.tum.ase.kleo.android.client.dto.GroupDTO;

public class StudentGroupsFragment extends Fragment {

    private GroupsApi groupsApi;

    private ProgressBar progressBar;
    private AlphaAnimation progressBarFadeInAnimation;
    private AlphaAnimation progressBarFadeOutAnimation;

    private RecyclerView studentGroupsListView;

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
        final View view = inflater.inflate(R.layout.fragment_student_groups, container, false);
        progressBar = view.findViewById(R.id.studentGroupsProgressBar);
        studentGroupsListView = view.findViewById(R.id.studentGroupsList);
        final LinearLayoutManager studentGroupsListLayoutManager = new LinearLayoutManager(view.getContext());
        studentGroupsListView.setLayoutManager(studentGroupsListLayoutManager);

        showLoadingProgressBar();
        new Thread(() -> {
            try {
                final List<GroupDTO> studentGroups = groupsApi.getGroups().execute().body();
                populateStudentGroupsListView(studentGroups);
            } catch (IOException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                hideLoadingProgressBar();
            }
        }).start();

        return view;
    }

    private void populateStudentGroupsListView(List<GroupDTO> groups) {
        getActivity().runOnUiThread(() -> {
            final RecyclerView.Adapter<?> studentGroupsAdapter = new StudentGroupsAdapter(groups);
            studentGroupsListView.setAdapter(studentGroupsAdapter);
        });
    }

    private void showLoadingProgressBar() {
        getActivity().runOnUiThread(() -> {
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
        });
    }

    private void hideLoadingProgressBar() {
        getActivity().runOnUiThread(() -> {
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
        });
    }
}
