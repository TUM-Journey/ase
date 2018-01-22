package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;

public class GroupAdvertisementScannerAdapter extends RecyclerView.Adapter<GroupAdvertisementScannerAdapter.GroupAdListItem> {

    private final List<Pair<BluetoothDevice, GroupDTO>> bluetoothDeviceGroups = new ArrayList<>();
    private GroupSessionOnClickListener groupSessionOnClickListener;

    public void appendAdvertisement(Pair<BluetoothDevice, GroupDTO> bluetoothDeviceGroup) {
        bluetoothDeviceGroups.add(bluetoothDeviceGroup);
        notifyDataSetChanged();
    }

    @Override
    public GroupAdListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_advertisement_scanner_item, parent, false);

        return new GroupAdListItem(view);
    }

    @Override
    public void onBindViewHolder(GroupAdListItem holder, int position) {
        final Pair<BluetoothDevice, GroupDTO> bluetoothDeviceGroup = bluetoothDeviceGroups.get(position);
        final BluetoothDevice device = bluetoothDeviceGroup.first;
        final GroupDTO group = bluetoothDeviceGroup.second;

        final OffsetDateTime now = OffsetDateTime.now();
        final SessionDTO nextNearestSession = group.getSessions().stream()
                .filter(session -> session.getBegins().isAfter(now))
                .findFirst().orElse(null);

        holder.setName(group.getName());
        holder.setSessionTime(nextNearestSession.getBegins().toLocalTime(),
                nextNearestSession.getEnds().toLocalTime());
        holder.setSessionType(nextNearestSession.getType().toString());
        holder.setSessionLocation(nextNearestSession.getLocation());

        if (groupSessionOnClickListener != null) {
            holder.itemView.setOnClickListener(e -> {
                groupSessionOnClickListener.onGroupSessionOnClick(device, group, nextNearestSession);
            });
        }
    }

    public void setGroupSessionOnClickListener(GroupSessionOnClickListener groupSessionOnClickListener) {
        this.groupSessionOnClickListener = groupSessionOnClickListener;
    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceGroups.size();
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

    public interface GroupSessionOnClickListener {
        void onGroupSessionOnClick(BluetoothDevice bluetoothDevice, GroupDTO group, SessionDTO sessionId);
    }
}
