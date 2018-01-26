package de.tum.ase.kleo.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.Principal;
import de.tum.ase.kleo.app.group.GroupListFragment;
import de.tum.ase.kleo.app.group.attendance.GroupAttendanceVerifierFragment;
import de.tum.ase.kleo.app.group.attendance.advertisement.GroupAdvertisementBroadcasterFragment;
import de.tum.ase.kleo.app.group.attendance.advertisement.GroupAdvertisementScannerFragment;
import de.tum.ase.kleo.app.user.UserAttendanceListFragment;
import de.tum.ase.kleo.app.user.UserListFragment;
import io.reactivex.Completable;

import static de.tum.ase.kleo.app.MainMenu.Page;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity {

    private BackendClient backendClient;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backendClient = ((KleoApplication) getApplication()).backendClient();
        drawer = findViewById(R.id.drawer_layout);

        final Principal principal = backendClient.principal();
        final NavigationView navigationView = findViewById(R.id.main_nav_view);
        applyMenuAccessRules(navigationView.getMenu(), principal);
        populateNavigationViewHeader(navigationView, principal);
        setupNavigationViewMenu(navigationView);

        setupToolbar(findViewById(R.id.main_toolbar));
    }

    private void setupToolbar(Toolbar toolbar) {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.main_menu_open_msg, R.string.main_menu_close_msg);

        setSupportActionBar(toolbar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void applyMenuAccessRules(Menu menu, Principal principal) {
        if (!principal.isStudent()) {
            menu.findItem(R.id.menu_student).setVisible(false);
        }
        if (!principal.isTutor()) {
            menu.findItem(R.id.menu_tutor).setVisible(false);
        }
        if (!principal.isSuperuser()) {
            menu.findItem(R.id.menu_superuser).setVisible(false);
        }
    }

    private void populateNavigationViewHeader(NavigationView navigationView, Principal principal) {
        final View headerView = navigationView.getHeaderView(0);

        final TextView usernameView
                = headerView.findViewById(R.id.main_menu_header_user_name_txt);
        final TextView emailView
                = headerView.findViewById(R.id.main_menu_header_user_email_txt);
        final TextView studentIdView
                = headerView.findViewById(R.id.main_menu_header_user_studentid_txt);

        usernameView.setText(principal.name());
        emailView.setText(principal.email());
        if (principal.studentId() != null) {
            studentIdView.setText(principal.studentId());
        }
    }

    private MainMenu setupNavigationViewMenu(NavigationView navigationView) {
        return new MainMenu.Builder()
                .defaultPage(Page().from(new WelcomeFragment()).noBackStack())
                .use(getFragmentManager())
                .use(navigationView)
                .setOnClickPageChange(
                        R.id.menu_group_advertisement_scanner,
                        Page().from(new GroupAdvertisementScannerFragment()).defaultBackStack())
                .setOnClickPageChange(
                        R.id.menu_group_list,
                        Page().from(new GroupListFragment()).defaultBackStack())
                .setOnClickPageChange(
                        R.id.menu_attendance_blockchain,
                        Page().from(new GroupAttendanceVerifierFragment()).defaultBackStack())
                .setOnClickPageChange(
                        R.id.menu_user_attendance,
                        Page().from(new UserAttendanceListFragment()).defaultBackStack())
                .setOnClickPageChange(
                        R.id.menu_group_advertisement_broadcaster,
                        Page().from(new GroupAdvertisementBroadcasterFragment()).defaultBackStack())
                .setOnClickPageChange(
                        R.id.menu_user_list,
                        Page().from(new UserListFragment()).defaultBackStack())
                .setOnClickAction(
                        R.id.menu_logout, () -> {
                            backendClient.logout();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        })
                .setBeforeTriggerListener(menuItem -> closeDrawer())
                .setAfterTriggerListener((menuItem) ->
                        Completable.fromAction(()
                                -> setActionBarTitle(menuItem.getTitle())))
        .build();
    }

    private Completable closeDrawer() {
        return Completable.create(emitter -> {
            drawer.closeDrawer(GravityCompat.START);
            drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    emitter.onComplete();
                    drawer.removeDrawerListener(this);
                }

                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
                @Override
                public void onDrawerOpened(@NonNull View drawerView) {}
                @Override
                public void onDrawerStateChanged(int newState) {}
            });
        });
    }

    private boolean setActionBarTitle(CharSequence name) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return false;

        getSupportActionBar().setTitle(name);
        return true;
    }
}
