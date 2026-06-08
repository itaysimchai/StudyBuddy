package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.adapter.SessionsAdapter;
import com.example.studybuddy.data.FakeData;
import com.example.studybuddy.model.Session;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private RecyclerView rvSessions;
    private SessionsAdapter adapter;
    private ArrayList<Session> sessionList;

    private EditText etSearch;
    private Button btnNavHome, btnNavProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvSessions = findViewById(R.id.rvSessions);
        etSearch = findViewById(R.id.etSearch);
        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavProfile = findViewById(R.id.btnNavProfile);

        db = FirebaseFirestore.getInstance();

        sessionList = new ArrayList<>();

        adapter = new SessionsAdapter(this, new ArrayList<>());
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(adapter);

        loadSessionsFromFirestore();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSessions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnNavHome.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "You are already on Home", Toast.LENGTH_SHORT).show()
        );

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadSessionsFromFirestore() {
        db.collection("Sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Session session = document.toObject(Session.class);
                        sessionList.add(session);
                    }

                    adapter.updateList(new ArrayList<>(sessionList));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Firebase Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    sessionList = FakeData.createSessions();
                    adapter.updateList(new ArrayList<>(sessionList));
                });
    }

    private void filterSessions(String query) {
        ArrayList<Session> filteredList = new ArrayList<>();

        for (Session session : sessionList) {
            String courseName = session.getCourseName() == null ? "" : session.getCourseName();
            String topic = session.getTopic() == null ? "" : session.getTopic();

            if (courseName.toLowerCase().contains(query.toLowerCase()) ||
                    topic.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(session);
            }
        }

        adapter.updateList(filteredList);
    }
}