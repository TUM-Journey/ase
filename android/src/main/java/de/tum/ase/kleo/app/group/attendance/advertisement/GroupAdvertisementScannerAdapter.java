package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleTime;

public class GroupAdvertisementScannerAdapter
        extends RecyclerView.Adapter<GroupAdvertisementScannerAdapter.GroupAdListItem> {

    private final List<AdvertisementRecord> advertisementRecords = new ArrayList<>();
    private GroupSessionOnClickListener groupSessionOnClickListener;

    public void appendActiveAdvertisement(BluetoothDevice device, GroupDTO group) {
        advertisementRecords.add(new AdvertisementRecord(device, group, true));
        notifyDataSetChanged();
    }

    public void appendInactiveAdvertisement(BluetoothDevice device, GroupDTO group) {
        advertisementRecords.add(new AdvertisementRecord(device, group, false));
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
        final AdvertisementRecord advertisementRecord = advertisementRecords.get(position);
        final BluetoothDevice device = advertisementRecord.device;
        final GroupDTO group = advertisementRecord.group;
        final boolean isActive = advertisementRecord.isActive;

        final OffsetDateTime now = OffsetDateTime.now();
        final SessionDTO nextNearestSession = group.getSessions().stream()
                .filter(session -> session.getBegins().isAfter(now))
                .findFirst().orElse(null);

        holder.setName(group.getName());
        holder.setSessionTime(nextNearestSession.getBegins().toLocalTime(),
                nextNearestSession.getEnds().toLocalTime());
        holder.setSessionType(nextNearestSession.getType().toString());
        holder.setSessionLocation(nextNearestSession.getLocation());

        if (!isActive) {
            holder.grayout();
            holder.itemView.setOnClickListener(e -> {
                Toast.makeText(holder.itemView.getContext(),
                        R.string.group_ad_scanner_item_warning_attend_not_registered_toast,
                            Toast.LENGTH_LONG).show();
            });
        } else if (groupSessionOnClickListener != null) {
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
        return advertisementRecords.size();
    }

    static class GroupAdListItem extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView sessionTime;
        private TextView sessionLocation;
        private TextView sessionType;

        GroupAdListItem(View view) {
            super(view);

            name = view.findViewById(R.id.group_ad_scanner_list_item_name_txt);
            sessionTime = view.findViewById(R.id.group_ad_scanner_list_item_session_time_txt);
            sessionType = view.findViewById(R.id.group_ad_scanner_list_item_session_type_txt);
            sessionLocation = view.findViewById(R.id.group_ad_scanner_list_item_session_location);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setSessionTime(LocalTime begins, LocalTime ends) {
            final String sessionInterval = simpleTime(begins) + " - " + simpleTime(ends);

            this.sessionTime.setText(sessionInterval);
        }

        public void setSessionLocation(String location) {
            this.sessionLocation.setText(location);
        }

        public void setSessionType(String sessionType) {
            this.sessionType.setText(sessionType);
        }

        public void grayout() {
            name.setTextColor(Color.GRAY);
            sessionTime.setTextColor(Color.GRAY);
            sessionType.setTextColor(Color.GRAY);
            sessionLocation.setTextColor(Color.GRAY);
        }
    }

    public interface GroupSessionOnClickListener {
        void onGroupSessionOnClick(BluetoothDevice bluetoothDevice, GroupDTO group, SessionDTO sessionId);
    }

    private static class AdvertisementRecord {
        BluetoothDevice device;
        GroupDTO group;
        boolean isActive;

        AdvertisementRecord(BluetoothDevice device, GroupDTO group, boolean isActive) {
            this.device = device;
            this.group = group;
            this.isActive = isActive;
        }
    }
}
