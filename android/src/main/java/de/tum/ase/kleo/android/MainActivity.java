package de.tum.ase.kleo.android;

import android.app.Fragment;
import android.app.FragmentManager;
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

import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.client.Principal;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BackendClient backendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backendClient = ((KleoApplication) getApplication()).backendClient();
        final Principal principal = backendClient.principal();

        // Init action bar and drawer layout
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        setSupportActionBar(toolbar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup Navigation View
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        final TextView usernameView = headerView.findViewById(R.id.menuUserName);
        final TextView emailView = headerView.findViewById(R.id.menuUserEmail);
        final TextView studentIdView = headerView.findViewById(R.id.menuUserStudentId);

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
            case R.id.group_scanner:
                fragment = new GroupScannerFragment();
                break;
            case R.id.study_group:
                fragment = new StudyGroupsFragment();
                break;
            case R.id.student_registrations:
                fragment =new StudentRegistrationsFragment();
                break;
            case R.id.student_attendances:
                fragment = new StudentAttendancesFragment();
                break;
            case R.id.group_broadcaster:
                fragment = new GroupBroadcasterFragment();
                break;
            case R.id.user_management:
                fragment = new UserManagementFragment();
                break;
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
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.main_container, fragment)
                .commit();
    }
}
