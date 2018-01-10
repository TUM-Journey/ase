package de.tum.ase.kleo.android.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.android.client.dto.GroupDTO;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class StudentGroupsAdapter extends RecyclerView.Adapter<StudentGroupsAdapter.StudentGroupListItem> {

    private final List<GroupDTO> groups;
    private final boolean currentUserStudent;
    private final String currentUserId;
    private final BiConsumer<String, String> register;
    private final BiConsumer<String, String> deregister;

    public StudentGroupsAdapter(List<GroupDTO> groups, boolean currentUserStudent, String currentUserId,
                                BiConsumer<String, String> register, BiConsumer<String, String> deregister) {
        this.groups = groups;
        this.currentUserStudent = currentUserStudent;
        this.currentUserId = currentUserId;
        this.register = register;
        this.deregister = deregister;
    }

    @Override
    public StudentGroupListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_student_groups_list_item, parent, false);

        return new StudentGroupListItem(view);
    }

    @Override
    public void onBindViewHolder(StudentGroupListItem holder, int position) {
        final GroupDTO group = groups.get(position);

        final List<String> groupStudentIds = defaultIfNull(group.getStudentIds(), emptyList());

        holder.setGroupId(group.getId());
        holder.setName(group.getName());
        holder.setStudentsCount(groupStudentIds.size());
        holder.setRegistered(groupStudentIds.contains(currentUserId));
        holder.setRegisterSwitchEnabled(currentUserStudent);

        holder.setRegisterSwitchCallbacks(
                (groupId) -> register.accept(groupId, currentUserId),
                (groupId) -> deregister.accept(groupId, currentUserId));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class StudentGroupListItem extends RecyclerView.ViewHolder {

        private String groupId;
        private TextView name;
        private TextView students;
        private Switch registerSwitch;

        public StudentGroupListItem(View view) {
            super(view);

            name = view.findViewById(R.id.studentGroupsListItemName);
            students = view.findViewById(R.id.studentGroupsListItemStudents);
            registerSwitch = view.findViewById(R.id.studentGroupsListItemRegisterSwitch);
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setStudentsCount(int studentsCount) {
            this.students.setText(Integer.toString(studentsCount));
        }

        public void setRegistered(boolean checked) {
            this.registerSwitch.setChecked(checked);
        }

        public void setRegisterSwitchEnabled(boolean visible) {
            this.registerSwitch.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

        public void setRegisterSwitchCallbacks(Consumer<String> register, Consumer<String> deregister) {
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
