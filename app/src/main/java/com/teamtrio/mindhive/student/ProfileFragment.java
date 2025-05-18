package com.teamtrio.mindhive.student;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamtrio.mindhive.R;
import com.teamtrio.mindhive.common.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvPhone, tvAvatar, tvRole;
    private MaterialCardView profileCard;
    private LottieAnimationView lottieAnimation;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvUsername = view.findViewById(R.id.tv_profile_username);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvPhone = view.findViewById(R.id.tv_profile_phone);
        tvAvatar = view.findViewById(R.id.tv_profile_avatar);
        tvRole = view.findViewById(R.id.tv_profile_role);
        profileCard = view.findViewById(R.id.profile_card);
        lottieAnimation = view.findViewById(R.id.lottie_profile);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> logout());

        // Initially hide profile card
        profileCard.setAlpha(0f);

        loadCurrentUserProfile();

        return view;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        // Clear saved session if any
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        // Navigate to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadCurrentUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String role = documentSnapshot.getString("role");

                        tvUsername.setText("Username: " + (username != null ? username : "N/A"));
                        tvEmail.setText("Email: " + (email != null ? email : "N/A"));
                        tvPhone.setText("Phone: " + (phone != null ? phone : "N/A"));
                        tvRole.setText("Role: " + (role != null ? role : "N/A"));

                        // Avatar logic: emoji, first letter, or fallback
                        if (username != null && !username.isEmpty()) {
                            String avatar = username.trim().substring(0, 1).toUpperCase();
                            tvAvatar.setText(avatar);
                        } else {
                            tvAvatar.setText("?");
                        }

                        // Animate profile card fade-in
                        profileCard.animate().alpha(1f).setDuration(500).start();
                    } else {
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
