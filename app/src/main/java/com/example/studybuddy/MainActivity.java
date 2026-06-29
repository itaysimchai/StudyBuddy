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
import com.example.studybuddy.model.Session;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;

    private SessionsAdapter adapter;
    private ArrayList<Session> sessionList;

    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        RecyclerView rvSessions = findViewById(R.id.rvSessions);
        etSearch = findViewById(R.id.etSearch);
        Button btnNavHome = findViewById(R.id.btnNavHome);
        Button btnCreateSession = findViewById(R.id.btnCreateSession);
        Button btnNearbySessions = findViewById(R.id.btnNearbySessions);
        Button btnNavProfile = findViewById(R.id.btnNavProfile);

        sessionList = new ArrayList<>();
        adapter = new SessionsAdapter(this, new ArrayList<>());
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSessions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnNavHome.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "You are already on Home", Toast.LENGTH_SHORT).show()
        );

        btnCreateSession.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateSessionActivity.class))
        );

        btnNearbySessions.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NearbySessionsActivity.class))
        );

        btnNavProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessionsFromFirestore();
    }

    private void loadSessionsFromFirestore() {
        db.collection("Sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Session session = document.toObject(Session.class);
                        session.setDocumentId(document.getId());
                        sessionList.add(session);
                    }

                    adapter.updateList(new ArrayList<>(sessionList));
                    analytics.logEvent("sessions_loaded", null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Firebase Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    adapter.updateList(new ArrayList<>());
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
