package com.teamtrio.mindhive.instructor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.teamtrio.mindhive.R;

public class InstructorpanelActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instructorpanel);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container_ins),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Load default fragment (HomeFragment)
        loadFragment(new HomeFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_add_course) {
                fragment = new AddCourseFragment();
            } else if (id == R.id.nav_my_courses) {
                fragment = new MyCoursesFragment();
            } else if (id == R.id.nav_profile_ins) {
                fragment = new ProfileFragment_ins();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;  // Return true to indicate the event is handled
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_ins, fragment)
                .commit();
    }
}
