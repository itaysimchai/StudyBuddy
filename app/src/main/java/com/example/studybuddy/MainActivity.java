package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.adapter.SessionsAdapter;
import com.example.studybuddy.data.FakeData;
import com.example.studybuddy.model.Session;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

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

        sessionList = FakeData.createSessions();

        adapter = new SessionsAdapter(this, new ArrayList<>(sessionList));
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

        btnNavHome.setOnClickListener(v -> {
            // already here
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void filterSessions(String query) {
        ArrayList<Session> newFilteredList = new ArrayList<>();

        for (Session session : sessionList) {
            if (session.getCourseName().toLowerCase().contains(query.toLowerCase()) ||
                    session.getTopic().toLowerCase().contains(query.toLowerCase())) {
                newFilteredList.add(session);
            }
        }

        adapter.updateList(newFilteredList);
    }
}