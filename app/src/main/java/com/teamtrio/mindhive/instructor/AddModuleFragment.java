package com.teamtrio.mindhive.instructor;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teamtrio.mindhive.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddModuleFragment extends Fragment implements QuizCreatorFragment.QuizCreatorListener {

    private EditText etTitle, etDesc, etTheory;
    private TextView tvCourseTitle, tvVideoStatus, tvQuizStatus;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private String courseId, courseTitle;
    private Uri selectedVideoUri = null;
    private String videoDownloadUrl = null;
    private List<Map<String, Object>> quizQuestions = new ArrayList<>();

    public AddModuleFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_module, container, false);

        etTitle = view.findViewById(R.id.et_module_title);
        etDesc = view.findViewById(R.id.et_module_description);
        etTheory = view.findViewById(R.id.et_theory);
        tvCourseTitle = view.findViewById(R.id.tv_course_title);
        tvVideoStatus = view.findViewById(R.id.tv_video_status);
        tvQuizStatus = view.findViewById(R.id.tv_quiz_status);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            courseTitle = getArguments().getString("courseTitle");
            if (!TextUtils.isEmpty(courseTitle)) {
                tvCourseTitle.setText("Course: " + courseTitle);
            }
        }

        view.findViewById(R.id.btn_upload_video).setOnClickListener(v -> selectVideoFromStorage());
        view.findViewById(R.id.btn_create_quiz).setOnClickListener(v -> openQuizCreator());
        view.findViewById(R.id.btn_add_module).setOnClickListener(v -> addModule());

        return view;
    }

    private void selectVideoFromStorage() {
        // Implementation for selecting video from storage (e.g. startActivityForResult)
        // You need to implement video selection or use modern ActivityResult API.
    }

    private void openQuizCreator() {
        QuizCreatorFragment quizFragment = new QuizCreatorFragment();
        quizFragment.setQuizCreatorListener(this);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.quiz_fragment_container, quizFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onQuizCreated(List<Map<String, Object>> quizQuestions) {
        this.quizQuestions = quizQuestions;
        if (quizQuestions != null && !quizQuestions.isEmpty()) {
            tvQuizStatus.setText("Quiz created with " + quizQuestions.size() + " questions");
        } else {
            tvQuizStatus.setText("No quiz created");
        }
        // Optionally pop back the quiz fragment:
        getChildFragmentManager().popBackStack();
    }

    private void addModule() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String theory = etTheory.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(getContext(), "Title & Description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String moduleId = db.collection("courses").document(courseId)
                .collection("modules").document().getId();

        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("moduleId", moduleId);
        moduleData.put("title", title);
        moduleData.put("description", desc);
        moduleData.put("theory", theory);
        moduleData.put("videoLink", videoDownloadUrl);
        moduleData.put("quiz", quizQuestions);
        moduleData.put("createdAt", System.currentTimeMillis());

        db.collection("courses").document(courseId)
                .collection("modules").document(moduleId)
                .set(moduleData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Module added", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add module", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void clearInputs() {
        etTitle.setText("");
        etDesc.setText("");
        etTheory.setText("");
        tvVideoStatus.setText("No video selected");
        tvQuizStatus.setText("No quiz created");
        selectedVideoUri = null;
        videoDownloadUrl = null;
        quizQuestions.clear();
    }
}
