package com.teamtrio.mindhive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, phoneInput, passwordInput;
    private Button signupButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // No need to enable offline persistence manually, Firestore handles it by default

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        passwordInput = findViewById(R.id.password_input);
        signupButton = findViewById(R.id.signup_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        signupButton.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Prepare user data
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("role", "student");

                        // Save to Firestore
                        DocumentReference userRef = firestore.collection("users").document(uid);
                        userRef.set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    // Save user data locally (offline) in SharedPreferences
                                    saveSessionData(uid, username, email, phone, "student");

                                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, StudentdashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Function to save session data in SharedPreferences
    private void saveSessionData(String uid, String username, String email, String phone, String role) {
        // Save session data in SharedPreferences or any other local storage for offline access
        getSharedPreferences("user_session", MODE_PRIVATE)
                .edit()
                .putString("uid", uid)
                .putString("username", username)
                .putString("email", email)
                .putString("phone", phone)
                .putString("role", role)
                .apply();
    }

    // Function to check if the user is already logged in on app startup (using SharedPreferences)
    public static boolean isUserLoggedIn(AppCompatActivity activity) {
        return activity.getSharedPreferences("user_session", MODE_PRIVATE)
                .contains("uid");  // Checks if the "uid" is stored in SharedPreferences
    }
}
