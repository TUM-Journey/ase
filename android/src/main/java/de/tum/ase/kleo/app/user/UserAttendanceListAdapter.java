package de.tum.ase.kleo.app.user;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.OffsetDateTime;
import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.AttendanceDTO;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleDateTime;

public class UserAttendanceListAdapter extends RecyclerView.Adapter<UserAttendanceListAdapter.GroupAttendanceListItem> {

    private final List<AttendanceDTO> attendances;

    public UserAttendanceListAdapter(List<AttendanceDTO> attendances) {
        this.attendances = attendances;
    }

    @Override
    public GroupAttendanceListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user_attendance_list_item, parent, false);

        return new GroupAttendanceListItem(view);
    }

    @Override
    public void onBindViewHolder(GroupAttendanceListItem holder, int position) {
        final AttendanceDTO attendance = attendances.get(position);

        holder.setDate(attendance.getAttendedAt());
        holder.setName(attendance.getGroup().getName());
        holder.setSessionType(attendance.getSession().getType().toString());
    }

    @Override
    public int getItemCount() {
        return attendances.size();
    }

    static class GroupAttendanceListItem extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView date;
        private TextView sessionType;

        GroupAttendanceListItem(View view) {
            super(view);

            name = view.findViewById(R.id.group_attendance_list_item_name_txt);
            date = view.findViewById(R.id.group_attendance_list_item_date_txt);
            sessionType = view.findViewById(R.id.group_attendance_list_item_session_type_txt);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setDate(OffsetDateTime date) {
            this.date.setText(simpleDateTime(date));
        }

        public void setSessionType(String sessionType) {
            this.sessionType.setText(sessionType);
        }
    }
}
