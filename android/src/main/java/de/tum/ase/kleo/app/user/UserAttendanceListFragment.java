package de.tum.ase.kleo.app.user;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.Principal;
import de.tum.ase.kleo.app.client.StudentsApi;
import de.tum.ase.kleo.app.client.dto.AttendanceDTO;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeOut;

public class UserAttendanceListFragment extends ReactiveLayoutFragment {

    private StudentsApi studentApi;
    private RecyclerView listView;
    private Principal currentUser;

    public UserAttendanceListFragment() {
        super(R.layout.fragment_user_attendance_list);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        studentApi = backendClient.as(StudentsApi.class);
        currentUser = backendClient.principal().blockingGet();
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        final ProgressBar progressBar = view.findViewById(R.id.group_attendance_list_progressbar);
        listView = view.findViewById(R.id.group_attendance_list_view);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final Disposable groupsAttendancesReq = studentApi.getStudentAttendances(currentUser.id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> fadeIn(progressBar))
                .doFinally(() -> fadeOut(progressBar))
                .subscribe(this::populateGroupAttendanceListView, this::showError);

        disposeOnDestroy(groupsAttendancesReq);
    }

    private void populateGroupAttendanceListView(List<AttendanceDTO> attendances) {
        listView.setAdapter(new UserAttendanceListAdapter(attendances));
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
