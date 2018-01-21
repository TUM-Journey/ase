package de.tum.ase.kleo.app.group.attendance;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.AttendanceDTO;

public class GroupAttendanceAdapter extends RecyclerView.Adapter<GroupAttendanceAdapter.GroupAttendanceListItem> {

    private final List<AttendanceDTO> attendances;

    public GroupAttendanceAdapter(List<AttendanceDTO> attendances) {
        this.attendances = attendances;
    }

    @Override
    public GroupAttendanceListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_attendance_item, parent, false);

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

        // Example: Sat, Jan 20 at 09:34
        private static final DateTimeFormatter attendedAtTimeFormat
                = DateTimeFormatter.ofPattern("EEE, MMM d 'at' HH:mm", Locale.ENGLISH);

        private TextView name;
        private TextView date;
        private TextView sessionType;

        GroupAttendanceListItem(View view) {
            super(view);

            name = view.findViewById(R.id.groupAttendanceName);
            date = view.findViewById(R.id.groupAttendanceDate);
            sessionType = view.findViewById(R.id.groupAttendanceSessionType);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setDate(OffsetDateTime date) {
            this.date.setText(date.toLocalDateTime().format(attendedAtTimeFormat));
        }

        public void setSessionType(String sessionType) {
            this.sessionType.setText(sessionType);
        }
    }
}
