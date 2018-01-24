package de.tum.ase.kleo.app.user;

import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.StudentsApi;
import de.tum.ase.kleo.app.client.dto.AttendanceDTO;
import de.tum.ase.kleo.app.support.ui.ResourceListLayoutFragment;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleDateTime;

public class UserAttendanceListFragment extends ResourceListLayoutFragment<AttendanceDTO> {

    public UserAttendanceListFragment() {
        super(R.layout.fragment_user_attendance_list,
                R.id.user_attendance_list_view,
                R.layout.fragment_user_attendance_list_item,
                R.id.user_attendance_list_progressbar,
                R.id.user_attendance_list_no_records);
    }

    @Override
    protected Observable<List<AttendanceDTO>> fetchResources() {
        return backendClient.as(StudentsApi.class)
                .getStudentAttendances(backendClient.principal().id());
    }

    @Override
    protected void populateListItem(View view, AttendanceDTO attendance) {
        final TextView name = view.findViewById(R.id.group_attendance_list_item_name_txt);
        final TextView date = view.findViewById(R.id.group_attendance_list_item_date_txt);
        final TextView sessionType = view.findViewById(R.id.group_attendance_list_item_session_type_txt);

        name.setText(attendance.getGroup().getName());
        date.setText(simpleDateTime(attendance.getAttendedAt()));
        sessionType.setText(attendance.getSession().getType().toString());
    }
}
