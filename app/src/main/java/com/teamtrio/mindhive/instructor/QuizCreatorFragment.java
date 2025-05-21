package com.teamtrio.mindhive.instructor;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.teamtrio.mindhive.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizCreatorFragment extends Fragment {

    private LinearLayout questionsContainer;
    private Button btnAddQuestion, btnSaveQuiz;

    private List<QuestionViewHolder> questionViewHolders = new ArrayList<>();

    private QuizCreatorListener listener;

    public interface QuizCreatorListener {
        void onQuizCreated(List<Map<String, Object>> quizQuestions);
    }

    public QuizCreatorFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_quiz_creator, container, false);

        questionsContainer = view.findViewById(R.id.questions_container);
        btnAddQuestion = view.findViewById(R.id.btn_add_question);
        btnSaveQuiz = view.findViewById(R.id.btn_save_quiz);

        btnAddQuestion.setOnClickListener(v -> addQuestionView());

        btnSaveQuiz.setOnClickListener(v -> saveQuiz());

        addQuestionView();

        return view;
    }

    public void setQuizCreatorListener(QuizCreatorListener listener) {
        this.listener = listener;
    }

    private void addQuestionView() {
        View questionView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_quiz_question, questionsContainer, false);

        EditText etQuestion = questionView.findViewById(R.id.et_question);
        EditText etOption1 = questionView.findViewById(R.id.et_option_1);
        EditText etOption2 = questionView.findViewById(R.id.et_option_2);
        EditText etOption3 = questionView.findViewById(R.id.et_option_3);
        EditText etOption4 = questionView.findViewById(R.id.et_option_4);
        RadioGroup rgCorrectOption = questionView.findViewById(R.id.rg_correct_option);

        questionsContainer.addView(questionView);

        questionViewHolders.add(new QuestionViewHolder(etQuestion, etOption1, etOption2, etOption3, etOption4, rgCorrectOption));
    }

    private void saveQuiz() {
        List<Map<String, Object>> quizQuestions = new ArrayList<>();

        for (int i = 0; i < questionViewHolders.size(); i++) {
            QuestionViewHolder qvh = questionViewHolders.get(i);

            String questionText = qvh.etQuestion.getText().toString().trim();
            String option1 = qvh.etOption1.getText().toString().trim();
            String option2 = qvh.etOption2.getText().toString().trim();
            String option3 = qvh.etOption3.getText().toString().trim();
            String option4 = qvh.etOption4.getText().toString().trim();

            if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(option1) || TextUtils.isEmpty(option2) ||
                    TextUtils.isEmpty(option3) || TextUtils.isEmpty(option4)) {
                Toast.makeText(getContext(), "Please fill all fields for question " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedRadioButtonId = qvh.rgCorrectOption.getCheckedRadioButtonId();
            if (selectedRadioButtonId == -1) {
                Toast.makeText(getContext(), "Please select correct answer for question " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            View selectedRadioButton = qvh.rgCorrectOption.findViewById(selectedRadioButtonId);
            int correctOptionIndex = qvh.rgCorrectOption.indexOfChild(selectedRadioButton);

            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("question", questionText);
            List<String> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            options.add(option3);
            options.add(option4);
            questionMap.put("options", options);
            questionMap.put("correctAnswerIndex", correctOptionIndex);

            quizQuestions.add(questionMap);
        }

        if (listener != null) {
            listener.onQuizCreated(quizQuestions);
        }
    }

    private static class QuestionViewHolder {
        EditText etQuestion, etOption1, etOption2, etOption3, etOption4;
        RadioGroup rgCorrectOption;

        QuestionViewHolder(EditText etQuestion, EditText etOption1, EditText etOption2,
                           EditText etOption3, EditText etOption4, RadioGroup rgCorrectOption) {
            this.etQuestion = etQuestion;
            this.etOption1 = etOption1;
            this.etOption2 = etOption2;
            this.etOption3 = etOption3;
            this.etOption4 = etOption4;
            this.rgCorrectOption = rgCorrectOption;
        }
    }
}
