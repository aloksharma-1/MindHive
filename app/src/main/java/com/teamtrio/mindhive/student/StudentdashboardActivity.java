package com.teamtrio.mindhive.student;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamtrio.mindhive.common.LoginActivity;
import com.teamtrio.mindhive.R;


public class StudentdashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge content

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if not authenticated
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        setContentView(R.layout.activity_studentdashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load default fragment
        loadFragment(new CoursesFragment());

        // Setup navigation item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_courses) {
                fragment = new CoursesFragment();
            } else if (id == R.id.nav_assignments) {
                fragment = new AssignmentsFragment();
            } else if (id == R.id.nav_announcements) {
                fragment = new AnnouncementsFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

        return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_fragment, fragment)
                .commit();
        return true;
    }


}
