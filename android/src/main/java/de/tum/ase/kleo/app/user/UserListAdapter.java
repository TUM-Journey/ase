package de.tum.ase.kleo.app.user;

import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.UserDTO;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListItem> {

    private final List<UserDTO> users;
    private final String currentUserId;
    private final Consumer<String> userRemovalCallback;
    private final BiConsumer<String, List<UserDTO.RolesEnum>> userRolesChangeCallback;

    public UserListAdapter(List<UserDTO> users, String currentUserId,
                           Consumer<String> userRemovalCallback,
                           BiConsumer<String, List<UserDTO.RolesEnum>> userRolesChangeCallback) {
        this.users = users;
        this.currentUserId = currentUserId;
        this.userRemovalCallback = userRemovalCallback;
        this.userRolesChangeCallback = userRolesChangeCallback;
    }

    @Override
    public UserListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user_list_item, parent, false);
        return new UserListItem(view);
    }

    @Override
    public void onBindViewHolder(UserListItem holder, int position) {
        final UserDTO user = users.get(position);
        final boolean isCurrentUser = currentUserId.equals(user.getId());

        holder.setUserId(user.getId());
        holder.setName(user.getName());
        holder.setRoles(user.getRoles());
        holder.setUserActionsVisible(!isCurrentUser);
        holder.setIsCurrentUserLabelVisible(isCurrentUser);

        holder.setUserRolesChangeCallback(userRolesChangeCallback);
        holder.setUserRemovalCallback(userId -> {
            users.removeIf(usr -> usr.getId().equals(userId));
            notifyDataSetChanged();

            userRemovalCallback.accept(userId);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserListItem extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView isCurrentUser;
        private final TextView role;
        private final ImageButton changeRoleButton;
        private final ImageButton removeButton;

        private String userId;
        private List<UserDTO.RolesEnum> userRoles;

        UserListItem(View view) {
            super(view);

            name = view.findViewById(R.id.user_list_item_user_name_txt);
            isCurrentUser = view.findViewById(R.id.user_list_item_user_is_current_label_txt);
            role = view.findViewById(R.id.user_list_item_user_role_txt);

            changeRoleButton = view.findViewById(R.id.user_list_item_role_change_img_btn);
            removeButton = view.findViewById(R.id.user_list_item_remove_img_btn);
        }

        void setIsCurrentUserLabelVisible(boolean isVisible) {
            isCurrentUser.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setRoles(List<UserDTO.RolesEnum> roles) {
            this.userRoles = roles;
            final String rolesCsv = roles.stream()
                    .map(UserDTO.RolesEnum::toString)
                    .collect(joining(", "));

            this.role.setText(rolesCsv);
        }

        void setUserId(String userId) {
            this.userId = userId;
        }

        void setUserActionsVisible(boolean isVisible) {
            changeRoleButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            removeButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }

        void setUserRemovalCallback(Consumer<String> callback) {
            removeButton.setOnClickListener(v -> callback.accept(userId));
        }

        void setUserRolesChangeCallback(BiConsumer<String, List<UserDTO.RolesEnum>> callback) {
            changeRoleButton.setOnClickListener(v -> updateUserRoles(callback));
        }

        private void updateUserRoles(BiConsumer<String, List<UserDTO.RolesEnum>> callback) {
            final String[] roleEnumValues
                    = stream(UserDTO.RolesEnum.values())
                        .map(UserDTO.RolesEnum::getValue)
                        .toArray(String[]::new);
            final Boolean[] currentUserRolesChecked
                    = stream(UserDTO.RolesEnum.values())
                        .map(userRoles::contains)
                        .toArray(Boolean[]::new);

            final ArrayList<UserDTO.RolesEnum> newUserRoles = new ArrayList<>(userRoles);

            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.user_list_item_roles_change_popup_title)
                    .setMultiChoiceItems(roleEnumValues, toPrimitive(currentUserRolesChecked),
                            (dialog, which, isChecked) -> {
                                final UserDTO.RolesEnum userRoleChanged
                                        = UserDTO.RolesEnum.fromValue(roleEnumValues[which]);

                                if (newUserRoles.contains(userRoleChanged) && !isChecked) {
                                    newUserRoles.remove(userRoleChanged);
                                } else if (!newUserRoles.contains(userRoleChanged) && isChecked) {
                                    newUserRoles.add(userRoleChanged);
                                }
                            })
                    .setPositiveButton(R.string.user_list_item_roles_change_popup_title_save_changes, (dialog, which) -> {
                        setRoles(newUserRoles);
                        callback.accept(userId, newUserRoles);
                    }).show();
        }
    }
}
