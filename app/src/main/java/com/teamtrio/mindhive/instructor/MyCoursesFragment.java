package com.teamtrio.mindhive.instructor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import com.teamtrio.mindhive.R;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> courseTitles;

    private FirebaseFirestore firestore;
    private String instructorId;

    public MyCoursesFragment() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_courses, container, false);

        listView = view.findViewById(R.id.listViewCourses);
        courseTitles = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, courseTitles);
        listView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        instructorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCourses();

        return view;
    }

    private void loadCourses() {
        firestore.collection("courses")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseTitles.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        courseTitles.add(title);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
