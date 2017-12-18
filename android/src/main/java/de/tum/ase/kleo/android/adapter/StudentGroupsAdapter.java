package de.tum.ase.kleo.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.android.client.dto.GroupDTO;

public class StudentGroupsAdapter extends RecyclerView.Adapter<StudentGroupsAdapter.ViewHolder> {

    private final List<GroupDTO> groups;

    public StudentGroupsAdapter(List<GroupDTO> groups) {
        this.groups = groups;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_student_groups_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final GroupDTO group = groups.get(position);

        final String groupName = group.getName();
        final String groupStudentsAmount = group.getStudentIds() != null
                ? Integer.toString(group.getStudentIds().size()) : "0";

        holder.name.setText(groupName);
        holder.students.setText(groupStudentsAmount);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView students;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.studentGroupsListItemName);
            students = view.findViewById(R.id.studentGroupsListItemStudents);
        }
    }
}
