package com.teamtrio.mindhive;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge support for modern layouts
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Set login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        // Set forgot password click listener (Optional)
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Handle window insets (Edge-to-edge layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Function to handle user login
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar while logging in
        progressBar.setVisibility(View.VISIBLE);

        // Firebase sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (task.isSuccessful()) {
                        // Login successful, redirect user to appropriate panel
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndRedirect(user.getUid());
                        }
                    } else {
                        // Login failed, show error message
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Function to check user role and redirect accordingly (Admin, Instructor, Student)
    private void checkUserRoleAndRedirect(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Retrieve user data from Firebase Realtime Database
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);

                if (role != null) {
                    // Redirect based on user role
                    Intent intent;
                    switch (role) {
                        case "admin":
                            intent = new Intent(LoginActivity.this, AdminpanelActivity.class);
                            break;
                        case "instructor":
                            intent = new Intent(LoginActivity.this, InstructorpanelActivity.class);
                            break;
                        default:
                            intent = new Intent(LoginActivity.this, StudentdashboardActivity.class);
                            break;
                    }
                    startActivity(intent);
                    finish(); // Close the login screen
                } else {
                    Toast.makeText(LoginActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
