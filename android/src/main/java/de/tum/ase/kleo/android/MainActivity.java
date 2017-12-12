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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.client.Principal;
import de.tum.ase.kleo.android.studying.groups.StudentGroupsFragment;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BackendClient backendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backendClient = ((KleoApplication) getApplication()).backendClient();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        setSupportActionBar(toolbar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fillOutNavigationHeaderStudentInfo(navigationView.getHeaderView(0));

        setContent(new WelcomeFragment());
    }

    private void fillOutNavigationHeaderStudentInfo(View view) {
        final TextView usernameView = view.findViewById(R.id.menuUserName);
        final TextView emailView = view.findViewById(R.id.menuUserEmail);
        final TextView studentIdView = view.findViewById(R.id.menuUserStudentId);

        final Principal principal = backendClient.principal();

        usernameView.setText(principal.name());
        emailView.setText(principal.email());
        if (principal.studentId() != null) {
            studentIdView.setText(format("(%s)", principal.studentId()));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        Fragment fragment;
        if (id == R.id.group_scanner) {
            fragment = new GroupScannerFragment();
        } else if (id == R.id.student_groups) {
            fragment = new StudentGroupsFragment();
        } else if (id == R.id.student_registrations) {
            fragment = new StudentRegistrationsFragment();
        } else if (id == R.id.student_attendances) {
            fragment = new StudentAttendancesFragment();
        } else if (id == R.id.group_broadcaster) {
            fragment = new GroupBroadcasterFragment();
        } else if (id == R.id.tutoring_groups) {
            fragment = new TutoringGroupsFragment();
        } else {
            throw new IllegalStateException("Unknown menu choice");
        }

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
