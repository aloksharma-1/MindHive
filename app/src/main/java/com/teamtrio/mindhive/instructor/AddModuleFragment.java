package com.teamtrio.mindhive.instructor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.teamtrio.mindhive.R;

import java.util.HashMap;
import java.util.Map;

public class AddModuleFragment extends Fragment {

    private EditText etTitle, etDesc, etTheory, etVideo, etQuiz;
    private FirebaseFirestore db;
    private String courseId, courseTitle;

    public AddModuleFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_module, container, false);

        etTitle = view.findViewById(R.id.et_module_title);
        etDesc = view.findViewById(R.id.et_module_description);
        etTheory = view.findViewById(R.id.et_theory);
        etVideo = view.findViewById(R.id.et_video_link);
        etQuiz = view.findViewById(R.id.et_quiz);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            courseTitle = getArguments().getString("courseTitle");
        }

        view.findViewById(R.id.btn_add_module).setOnClickListener(v -> addModule());

        return view;
    }

    private void addModule() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String theory = etTheory.getText().toString().trim();
        String video = etVideo.getText().toString().trim();
        String quiz = etQuiz.getText().toString().trim();

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
        moduleData.put("videoLink", video);
        moduleData.put("quiz", quiz);
        moduleData.put("createdAt", System.currentTimeMillis());

        db.collection("courses").document(courseId)
                .collection("modules").document(moduleId)
                .set(moduleData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Module added", Toast.LENGTH_SHORT).show();

                    // Clear fields for next module
                    etTitle.setText("");
                    etDesc.setText("");
                    etTheory.setText("");
                    etVideo.setText("");
                    etQuiz.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add module", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
