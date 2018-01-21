package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;

public class GroupAdvertisementScannerAdapter extends RecyclerView.Adapter<GroupAdvertisementScannerAdapter.GroupAdListItem> {

    private final List<GroupDTO> groups = new ArrayList<>();

    public void appendGroup(GroupDTO groupDTO) {
        if (!groups.contains(groupDTO)) {
            groups.add(groupDTO);
            notifyDataSetChanged();
        }
    }

    public boolean hasGroup(String code) {
        return groups.stream().anyMatch(group -> group.getCode().equals(code));
    }

    @Override
    public GroupAdListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_advertisement_scanner_item, parent, false);

        return new GroupAdListItem(view);
    }

    @Override
    public void onBindViewHolder(GroupAdListItem holder, int position) {
        final GroupDTO group = groups.get(position);

        final OffsetDateTime now = OffsetDateTime.now();
        final SessionDTO nextNearestSession = group.getSessions().stream()
                .filter(session -> session.getBegins().isAfter(now))
                .findFirst().orElse(null);

        holder.setName(group.getName());
        holder.setSessionTime(nextNearestSession.getBegins().toLocalTime(),
                nextNearestSession.getEnds().toLocalTime());
        holder.setSessionType(nextNearestSession.getType().toString());
        holder.setSessionLocation(nextNearestSession.getLocation());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupAdListItem extends RecyclerView.ViewHolder {

        private static final DateTimeFormatter sessionTimeFormat
                = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        private TextView name;
        private TextView sessionTime;
        private TextView sessionLocation;
        private TextView sessionType;

        GroupAdListItem(View view) {
            super(view);

            name = view.findViewById(R.id.group_ad_scanner_list_item_name);
            sessionTime = view.findViewById(R.id.group_ad_scanner_list_item_session_time);
            sessionType = view.findViewById(R.id.group_ad_scanner_list_item_session_type);
            sessionLocation = view.findViewById(R.id.group_ad_scanner_list_item_session_location);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setSessionTime(LocalTime begins, LocalTime ends) {
            final String sessionInterval = begins.format(sessionTimeFormat)
                    + " - " + ends.format(sessionTimeFormat);

            this.sessionTime.setText(sessionInterval);
        }

        public void setSessionLocation(String location) {
            this.sessionLocation.setText(location);
        }

        public void setSessionType(String sessionType) {
            this.sessionType.setText(sessionType);
        }
    }
}
