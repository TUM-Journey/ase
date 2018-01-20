package de.tum.ase.kleo.app.group;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.function.Consumer;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.GroupDTO;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupListItem> {

    private final List<GroupDTO> groups;
    private final boolean registerSwitchEnabled;
    private final String currentUserId;
    private final Consumer<String> registrationCallback;
    private final Consumer<String> deregistrationCallback;

    private GroupListAdapter(List<GroupDTO> groups, boolean registerSwitchEnabled,
                             String currentUserId,
                             Consumer<String> registrationCallback,
                             Consumer<String> deregistrationCallback) {
        this.groups = groups;
        this.registerSwitchEnabled = registerSwitchEnabled;
        this.currentUserId = currentUserId;
        this.registrationCallback = registrationCallback;
        this.deregistrationCallback = deregistrationCallback;
    }

    public static GroupListAdapter withRegisterSwitchDisabled(List<GroupDTO> groups) {
        return new GroupListAdapter(groups, false, null, null, null);
    }

    public static GroupListAdapter withRegisterSwitchEnabled(List<GroupDTO> groups,
                                                             String currentUserId,
                                                             Consumer<String> registrationCallback,
                                                             Consumer<String> deregistrationCallback) {
        return new GroupListAdapter(groups, true, currentUserId, registrationCallback, deregistrationCallback);
    }

    @Override
    public GroupListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_list_item, parent, false);

        return new GroupListItem(view);
    }

    @Override
    public void onBindViewHolder(GroupListItem holder, int position) {
        final GroupDTO group = groups.get(position);

        final List<String> groupStudentIds = defaultIfNull(group.getStudentIds(), emptyList());

        holder.setGroupId(group.getId());
        holder.setName(group.getName());
        holder.setStudentsCount(groupStudentIds.size());

        if (registerSwitchEnabled) {
            holder.enableRegisterSwitch();
            holder.setRegistered(groupStudentIds.contains(currentUserId));
            holder.setRegisterSwitchCallbacks(this.registrationCallback, this.deregistrationCallback);
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupListItem extends RecyclerView.ViewHolder {

        private String groupId;
        private TextView name;
        private TextView students;
        private Switch registerSwitch;

        GroupListItem(View view) {
            super(view);

            name = view.findViewById(R.id.studentGroupsListItemName);
            students = view.findViewById(R.id.studentGroupsListItemStudents);
            registerSwitch = view.findViewById(R.id.studentGroupsListItemRegisterSwitch);

            registerSwitch.setVisibility(View.INVISIBLE);
        }

        void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setStudentsCount(int studentsCount) {
            this.students.setText(Integer.toString(studentsCount));
        }

        void setRegistered(boolean checked) {
            this.registerSwitch.setChecked(checked);
        }

        void enableRegisterSwitch() {
            this.registerSwitch.setVisibility(View.VISIBLE);
        }

        void disableRegisterSwitch() {
            registerSwitch.setVisibility(View.INVISIBLE);
        }

        void setRegisterSwitchCallbacks(Consumer<String> register, Consumer<String> deregister) {
            registerSwitch.setOnClickListener(v -> {
                final Integer oldStudentsCount = Integer.valueOf(students.getText().toString());

                if (registerSwitch.isChecked()) {
                    register.accept(groupId);
                    students.setText(Integer.toString(oldStudentsCount + 1));
                } else {
                    deregister.accept(groupId);
                    students.setText(Integer.toString(oldStudentsCount - 1));
                }
            });
        }
    }
}
