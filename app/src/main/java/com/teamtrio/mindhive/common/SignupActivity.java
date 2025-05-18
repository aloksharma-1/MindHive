package com.teamtrio.mindhive.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamtrio.mindhive.R;
import com.teamtrio.mindhive.student.StudentdashboardActivity;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, phoneInput, passwordInput;
    private Button signupButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private AlertDialog progressDialog;
    private View progressDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        passwordInput = findViewById(R.id.password_input);
        signupButton = findViewById(R.id.signup_button);

        setupProgressDialog();

        signupButton.setOnClickListener(v -> createAccount());
    }

    private void setupProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        progressDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);
        builder.setView(progressDialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();
    }

    private void createAccount() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Reset errors
        usernameInput.setError(null);
        emailInput.setError(null);
        phoneInput.setError(null);
        passwordInput.setError(null);

        boolean isValid = true;

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            isValid = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            isValid = false;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            phoneInput.setError("Enter a valid phone number");
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) return;

        if (!isConnectedToInternet()) {
            Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            return;
        }

        signupButton.setEnabled(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("role", "student");

                        DocumentReference userRef = firestore.collection("users").document(uid);
                        userRef.set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    signupButton.setEnabled(true);
                                    saveSessionData(uid, username, email, phone, "student");
                                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, StudentdashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    signupButton.setEnabled(true);
                                    Log.e("SignupActivity", "Firestore error", e);
                                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        progressDialog.dismiss();
                        signupButton.setEnabled(true);
                        Log.e("SignupActivity", "FirebaseAuth error", task.getException());
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Legacy fallback for Android 5.x and below
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        } else {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
    }

    private void saveSessionData(String uid, String username, String email, String phone, String role) {
        getSharedPreferences("user_session", MODE_PRIVATE)
                .edit()
                .putString("uid", uid)
                .putString("username", username)
                .putString("email", email)
                .putString("phone", phone)
                .putString("role", role)
                .apply();
    }

    public static boolean isUserLoggedIn(AppCompatActivity activity) {
        return activity.getSharedPreferences("user_session", MODE_PRIVATE)
                .contains("uid");
    }
}
