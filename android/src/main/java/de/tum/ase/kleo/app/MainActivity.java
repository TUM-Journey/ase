package de.tum.ase.kleo.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String GOTO_MAIN_NAVIGATE_BACK_STACK = "main_navigate";

    private BackendClient backendClient;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backendClient = ((KleoApplication) getApplication()).backendClient();
        final Principal principal = backendClient.principal();

        // Init action bar and drawer layout
        final Toolbar toolbar = findViewById(R.id.main_toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.main_menu_open_msg, R.string.main_menu_close_msg);

        setSupportActionBar(toolbar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup Navigation View
        final NavigationView navigationView = findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setupFragmentManagerBackStack(navigationView);

        // 1.1. Hide menu items from not eligible users in Navigation View
        final Menu menu = navigationView.getMenu();
        if (!principal.isStudent()) {
            menu.findItem(R.id.menu_student).setVisible(false);
        }
        if (!principal.isTutor()) {
            menu.findItem(R.id.menu_tutor).setVisible(false);
        }
        if (!principal.isSuperuser()) {
            menu.findItem(R.id.menu_superuser).setVisible(false);
        }

        // 1.2. Fill out Navigation View header with student info
        final View headerView = navigationView.getHeaderView(0);
        final TextView usernameView = headerView.findViewById(R.id.main_menu_header_user_name_txt);
        final TextView emailView = headerView.findViewById(R.id.main_menu_header_user_email_txt);
        final TextView studentIdView = headerView.findViewById(R.id.main_menu_header_user_studentid_txt);

        usernameView.setText(principal.name());
        emailView.setText(principal.email());
        if (principal.studentId() != null) {
            studentIdView.setText(format("(%s)", principal.studentId()));
        }

        // Set default view content to welcome text
        setContent(new WelcomeFragment());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        final CharSequence itemTitle = item.getTitle();

        Fragment fragment;
        switch (itemId) {
            case R.id.menu_welcome:
                fragment = new WelcomeFragment();
                break;
            case R.id.menu_group_advertisement_scanner:
                fragment = new GroupAdvertisementScannerFragment();
                break;
            case R.id.menu_group_list:
                fragment = new GroupListFragment();
                break;
            case R.id.menu_attendance_blockchain:
                fragment = new GroupAttendanceVerifierFragment();
                break;
            case R.id.menu_user_attendance:
                fragment = new UserAttendanceListFragment();
                break;
            case R.id.menu_group_advertisement_broadcaster:
                fragment = new GroupAdvertisementBroadcasterFragment();
                break;
            case R.id.menu_user_list:
                fragment = new UserListFragment();
                break;
            case R.id.menu_logout:
                backendClient.logout();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                throw new IllegalStateException("Unknown menu choice");
        }

        getSupportActionBar().setTitle(itemTitle);
        setContent(fragment);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setContent(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.main_container, fragment)
                .addToBackStack(GOTO_MAIN_NAVIGATE_BACK_STACK)
                .commit();
    }

    private void setupFragmentManagerBackStack(NavigationView navigationView) {
        getFragmentManager().addOnBackStackChangedListener(() -> {
            final Fragment currentFragment
                    = getFragmentManager().findFragmentById(R.id.main_container);

            if (currentFragment instanceof GroupAdvertisementScannerFragment) {
                navigationView.setCheckedItem(R.id.menu_group_advertisement_scanner);
            } else if (currentFragment instanceof GroupListFragment) {
                navigationView.setCheckedItem(R.id.menu_group_list);
            } else if (currentFragment instanceof GroupAttendanceVerifierFragment) {
                navigationView.setCheckedItem(R.id.menu_attendance_blockchain);
            } else if (currentFragment instanceof UserAttendanceListFragment) {
                navigationView.setCheckedItem(R.id.menu_user_attendance);
            } else if (currentFragment instanceof GroupAdvertisementBroadcasterFragment) {
                navigationView.setCheckedItem(R.id.menu_group_advertisement_broadcaster);
            } else if (currentFragment instanceof UserListFragment) {
                navigationView.setCheckedItem(R.id.menu_user_list);
            } else if (currentFragment instanceof WelcomeFragment) {
                navigationView.setCheckedItem(R.id.menu_welcome);
            }
        });
    }
}
