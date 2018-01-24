package de.tum.ase.kleo.app.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.UsersApi;
import de.tum.ase.kleo.app.client.dto.UserDTO;
import de.tum.ase.kleo.app.support.ui.ResourceListLayoutFragment;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

public class UserListFragment extends ResourceListLayoutFragment<UserDTO> {

    public UserListFragment() {
        super(R.layout.fragment_user_list,
                R.id.user_list_view,
                R.layout.fragment_user_list_item,
                R.id.user_list_progressbar,
                R.id.user_list_no_records);
    }

    private void removeUser(String userId) {
        final Disposable deleteUser = backendClient.as(UsersApi.class)
                .deleteUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .doOnError(this::showErrorMessage)
                .subscribe();

        disposeOnDestroy(deleteUser);
    }

    private void updateUserRoles(String userId, List<UserDTO.RolesEnum> newRoles) {
        final Disposable deleteUser = backendClient.as(UsersApi.class)
                .updateUserRoles(userId, userRolesToString(newRoles))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .doOnError(this::showErrorMessage)
                .subscribe();

        disposeOnDestroy(deleteUser);
    }

    // https://github.com/swagger-api/swagger-codegen/issues/7461
    private List<String> userRolesToString(List<UserDTO.RolesEnum> rolesEnums) {
        return rolesEnums.stream().map(Enum::name).collect(toList());
    }

    @Override
    protected Observable<List<UserDTO>> fetchResources() {
        return backendClient.as(UsersApi.class).getUsers();
    }

    @Override
    protected void populateListItem(View view, UserDTO user) {
        final TextView name = view.findViewById(R.id.user_list_item_user_name_txt);
        final TextView isCurrentUserLabel = view.findViewById(R.id.user_list_item_user_is_current_label_txt);
        final TextView role = view.findViewById(R.id.user_list_item_user_role_txt);

        final ImageButton changeRoleButton = view.findViewById(R.id.user_list_item_role_change_img_btn);
        final ImageButton removeButton = view.findViewById(R.id.user_list_item_remove_img_btn);

        final boolean isCurrentUserItem = backendClient.principal().id().equals(user.getId());

        name.setText(user.getName());
        role.setText(rolesToCsv(user.getRoles()));
        changeRoleButton.setVisibility(!isCurrentUserItem ? View.VISIBLE : View.INVISIBLE);
        removeButton.setVisibility(!isCurrentUserItem ? View.VISIBLE : View.INVISIBLE);
        isCurrentUserLabel.setVisibility(isCurrentUserItem ? View.VISIBLE : View.INVISIBLE);

        if (!isCurrentUserItem) {
            changeRoleButton.setOnClickListener(v ->
                    askForUserRolesUpdate(user.getRoles())
                            .subscribe(newRolesEnums -> {
                                updateUserRoles(user.getId(), newRolesEnums);
                                role.setText(rolesToCsv(newRolesEnums));
                            }));

            removeButton.setOnClickListener(v -> {
                removeUser(user.getId());
                removeResourceIf(resource -> resource.getId().equals(user.getId()));
            });
        }

    }

    private Single<List<UserDTO.RolesEnum>> askForUserRolesUpdate(List<UserDTO.RolesEnum> currentUserRoles) {
        return Single.create(emitter -> {
            final String[] roleEnumValues
                    = stream(UserDTO.RolesEnum.values())
                    .map(UserDTO.RolesEnum::getValue)
                    .toArray(String[]::new);
            final Boolean[] currentUserRolesChecked
                    = stream(UserDTO.RolesEnum.values())
                    .map(currentUserRoles::contains)
                    .toArray(Boolean[]::new);

            final List<UserDTO.RolesEnum> newUserRoles = new ArrayList<>(currentUserRoles);

            new AlertDialog.Builder(getContext())
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
                        emitter.onSuccess(newUserRoles);
                    })
                    .setCancelable(false)
                    .setOnDismissListener(dialog -> {
                        emitter.onSuccess(newUserRoles);
                    })
                    .show();
        });
    }

    private String rolesToCsv(List<UserDTO.RolesEnum> roles) {
        return roles.stream().map(UserDTO.RolesEnum::toString).collect(joining(", "));
    }
}
