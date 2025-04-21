package com.teamtrio.mindhive;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentdashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge layout support (if needed)
        EdgeToEdge.enable(this);

        // Check if the user is logged in using Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            Intent intent = new Intent(StudentdashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the current activity so the user can't return to this page
        } else {
            // User is logged in, proceed with the dashboard layout
            setContentView(R.layout.activity_studentdashboard);
        }
    }
}
