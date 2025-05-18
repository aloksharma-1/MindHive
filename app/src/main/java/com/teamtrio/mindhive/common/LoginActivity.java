package com.teamtrio.mindhive.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamtrio.mindhive.R;
import com.teamtrio.mindhive.admin.AdminpanelActivity;
import com.teamtrio.mindhive.instructor.InstructorpanelActivity;
import com.teamtrio.mindhive.student.StudentdashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView, signupRedirectTextView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 🔐 Auto-login if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences prefs = getSharedPreferences("MindHivePrefs", MODE_PRIVATE);
            String role = prefs.getString("user_role", null);

            if (role != null) {
                redirectToRoleActivity(role);
                return;
            } else {
                checkUserRoleAndRedirect(currentUser.getUid());
                return;
            }
        }

        // UI Setup
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progress_bar);
        signupRedirectTextView = findViewById(R.id.signup_redirect);

        loginButton.setOnClickListener(v -> loginUser());

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        signupRedirectTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 🔐 Login user with FirebaseAuth
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndRedirect(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 📥 Get role from Firestore, save to SharedPreferences, then redirect
    private void checkUserRoleAndRedirect(String userId) {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if (role != null) {
                            // Save to SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("MindHivePrefs", MODE_PRIVATE);
                            prefs.edit().putString("user_role", role).apply();

                            // Redirect
                            redirectToRoleActivity(role);
                        } else {
                            Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🚀 Redirect user to correct Activity
    private void redirectToRoleActivity(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminpanelActivity.class);
                break;
            case "instructor":
                intent = new Intent(this, InstructorpanelActivity.class);
                break;
            default:
                intent = new Intent(this, StudentdashboardActivity.class);
                break;
        }
        startActivity(intent);
        finish(); // Close login activity
    }
}
