package de.tum.ase.kleo.app.user;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.Principal;
import de.tum.ase.kleo.app.client.UsersApi;
import de.tum.ase.kleo.app.client.dto.UserDTO;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeOut;
import static java.util.stream.Collectors.toList;

public class UserListFragment extends ReactiveLayoutFragment {

    private UsersApi userApi;
    private RecyclerView listView;
    private Principal currentUser;

    public UserListFragment() {
        super(R.layout.fragment_user_list);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        userApi = backendClient.as(UsersApi.class);
        currentUser = backendClient.principal().blockingGet();
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final ProgressBar progressBar = view.findViewById(R.id.userListProgressBar);
        listView = view.findViewById(R.id.userList);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final Disposable groupsReq = userApi.getUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> fadeIn(progressBar))
                .doFinally(() -> fadeOut(progressBar))
                .subscribe(this::populateUserListView, this::showError);

        disposeOnDestroy(groupsReq);
    }

    private void populateUserListView(List<UserDTO> users) {
        listView.setAdapter(new UserListAdapter(users, currentUser.id(),
                this::removeUser, this::updateUserRoles));
    }

    private void removeUser(String userId) {
        final Disposable deleteUser = userApi.deleteUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Toast.makeText(getContext(),
                        getString(R.string.user_removed), Toast.LENGTH_LONG).show())
                .subscribe();

        disposeOnDestroy(deleteUser);
    }

    private void updateUserRoles(String userId, List<UserDTO.RolesEnum> newRoles) {
        final Disposable deleteUser = userApi.updateUserRoles(userId, userRolesToString(newRoles))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Toast.makeText(getContext(),
                        getString(R.string.user_roles_updated), Toast.LENGTH_LONG).show())
                .subscribe();

        disposeOnDestroy(deleteUser);
    }

    // https://github.com/swagger-api/swagger-codegen/issues/7461
    private List<String> userRolesToString(List<UserDTO.RolesEnum> rolesEnums) {
        return rolesEnums.stream().map(Enum::name).collect(toList());
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
