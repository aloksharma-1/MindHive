package com.teamtrio.mindhive.instructor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamtrio.mindhive.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCourseFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private TextInputEditText etCourseTitle, etCourseDescription;
    private Button btnUploadThumbnail;
    private ImageView imgThumbnailPreview;
    private MaterialButton btnSaveCourse;

    private Uri thumbnailUri;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    public AddCourseFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_course, container, false);

        etCourseTitle = view.findViewById(R.id.etCourseTitle);
        etCourseDescription = view.findViewById(R.id.etCourseDescription);
        btnUploadThumbnail = view.findViewById(R.id.btnUploadThumbnail);
        imgThumbnailPreview = view.findViewById(R.id.imgThumbnailPreview);
        btnSaveCourse = view.findViewById(R.id.btnSaveCourse);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("course_thumbnails");
        auth = FirebaseAuth.getInstance();

        btnUploadThumbnail.setOnClickListener(v -> openFileChooser());

        btnSaveCourse.setOnClickListener(v -> saveCourse());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Course Thumbnail"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            thumbnailUri = data.getData();

            // Load thumbnail preview using Glide
            Glide.with(this)
                    .load(thumbnailUri)
                    .into(imgThumbnailPreview);

            imgThumbnailPreview.setVisibility(View.VISIBLE);
        }
    }

    private void saveCourse() {
        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etCourseTitle.setError("Course title is required");
            etCourseTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etCourseDescription.setError("Course description is required");
            etCourseDescription.requestFocus();
            return;
        }

        // Disable save button to prevent multiple clicks
        btnSaveCourse.setEnabled(false);
        btnSaveCourse.setText("Saving...");

        String instructorEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "unknown";

        // Upload thumbnail if available, else save without thumbnail
        if (thumbnailUri != null) {
            String imageId = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child(imageId + ".jpg");

            imageRef.putFile(thumbnailUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String thumbnailUrl = uri.toString();
                                uploadCourseToFirestore(title, description, instructorEmail, thumbnailUrl);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to get thumbnail URL", Toast.LENGTH_SHORT).show();
                                resetSaveButton();
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload thumbnail", Toast.LENGTH_SHORT).show();
                        resetSaveButton();
                    });

        } else {
            // No thumbnail selected, just save course data
            uploadCourseToFirestore(title, description, instructorEmail, null);
        }
    }

    private void uploadCourseToFirestore(String title, String description, String instructorEmail, @Nullable String thumbnailUrl) {
        Map<String, Object> courseMap = new HashMap<>();
        courseMap.put("title", title);
        courseMap.put("description", description);
        courseMap.put("instructorEmail", instructorEmail);
        courseMap.put("createdAt", Timestamp.now());

        if (thumbnailUrl != null) {
            courseMap.put("thumbnailUrl", thumbnailUrl);
        }

        db.collection("courses")
                .add(courseMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Course created successfully!", Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                    // Navigate to AddModuleFragment
                    AddModuleFragment addModuleFragment = new AddModuleFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("courseId", documentReference.getId());
                    bundle.putString("courseTitle", title);
                    addModuleFragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_ins, addModuleFragment) // Replace with your real container ID
                            .addToBackStack(null)
                            .commit();
                    clearInputs();
                    // Optionally, navigate to add modules for this course using documentReference.getId()
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create course", Toast.LENGTH_SHORT).show();
                    resetSaveButton();
                    e.printStackTrace();
                });
    }

    private void resetSaveButton() {
        btnSaveCourse.setEnabled(true);
        btnSaveCourse.setText("Save Course");
    }

    private void clearInputs() {
        etCourseTitle.setText("");
        etCourseDescription.setText("");
        imgThumbnailPreview.setImageDrawable(null);
        imgThumbnailPreview.setVisibility(View.GONE);
        thumbnailUri = null;
    }
}
