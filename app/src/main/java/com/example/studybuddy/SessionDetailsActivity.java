package com.example.studybuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.adapter.ParticipantsAdapter;
import com.example.studybuddy.model.Session;
import com.example.studybuddy.model.Student;

import java.util.ArrayList;

public class SessionDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvInfo;
    private Button btnJoinLeave, btnBack;
    private RecyclerView rvParticipants;

    private Session session;
    private ParticipantsAdapter participantsAdapter;
    private boolean joined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvInfo = findViewById(R.id.tvInfo);
        btnJoinLeave = findViewById(R.id.btnJoinLeave);
        btnBack = findViewById(R.id.btnBack);
        rvParticipants = findViewById(R.id.rvParticipants);

        session = (Session) getIntent().getSerializableExtra("session");

        btnBack.setOnClickListener(v -> finish());

        if (session != null) {
            tvTitle.setText(session.getCourseName());
            tvInfo.setText(
                    session.getTopic() + "\n" +
                            session.getTime() + "\n" +
                            session.getLocation()
            );

            for (Student student : session.getParticipants()) {
                if (student.getName().equals("You")) {
                    joined = true;
                    break;
                }
            }

            if (joined) {
                btnJoinLeave.setText("Leave Session");
            } else {
                btnJoinLeave.setText("Join Session");
            }

            participantsAdapter = new ParticipantsAdapter(session.getParticipants());
            rvParticipants.setLayoutManager(new LinearLayoutManager(this));
            rvParticipants.setAdapter(participantsAdapter);
        }

        btnJoinLeave.setOnClickListener(v -> {
            if (session == null) return;

            if (!joined) {
                session.getParticipants().add(new Student("You", "Business Administration"));
                btnJoinLeave.setText("Leave Session");
                joined = true;
            } else {
                ArrayList<Student> participants = session.getParticipants();
                for (int i = 0; i < participants.size(); i++) {
                    if (participants.get(i).getName().equals("You")) {
                        participants.remove(i);
                        break;
                    }
                }
                btnJoinLeave.setText("Join Session");
                joined = false;
            }

            participantsAdapter.notifyDataSetChanged();
        });
    }
}