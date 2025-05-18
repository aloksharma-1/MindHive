package com.teamtrio.mindhive.instructor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.teamtrio.mindhive.R;

import java.util.HashMap;
import java.util.Map;

public class AddFinalExamFragment extends Fragment {

    private EditText etQuestion, etA, etB, etC, etD, etCorrect;
    private String courseId;
    private FirebaseFirestore db;

    public AddFinalExamFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_final_exam, container, false);

        etQuestion = view.findViewById(R.id.et_question);
        etA = view.findViewById(R.id.et_option_a);
        etB = view.findViewById(R.id.et_option_b);
        etC = view.findViewById(R.id.et_option_c);
        etD = view.findViewById(R.id.et_option_d);
        etCorrect = view.findViewById(R.id.et_correct_answer);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
        }

        view.findViewById(R.id.btn_add_question).setOnClickListener(v -> addQuestion());

        return view;
    }

    private void addQuestion() {
        String question = etQuestion.getText().toString().trim();
        String a = etA.getText().toString().trim();
        String b = etB.getText().toString().trim();
        String c = etC.getText().toString().trim();
        String d = etD.getText().toString().trim();
        String correct = etCorrect.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(a) || TextUtils.isEmpty(b)
                || TextUtils.isEmpty(c) || TextUtils.isEmpty(d) || TextUtils.isEmpty(correct)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!correct.matches("[ABCD]")) {
            Toast.makeText(getContext(), "Correct answer must be A, B, C, or D", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("question", question);
        questionData.put("optionA", a);
        questionData.put("optionB", b);
        questionData.put("optionC", c);
        questionData.put("optionD", d);
        questionData.put("correctAnswer", correct);

        db.collection("courses").document(courseId)
                .collection("final_exam")
                .add(questionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Question added", Toast.LENGTH_SHORT).show();
                    etQuestion.setText("");
                    etA.setText("");
                    etB.setText("");
                    etC.setText("");
                    etD.setText("");
                    etCorrect.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add question", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
