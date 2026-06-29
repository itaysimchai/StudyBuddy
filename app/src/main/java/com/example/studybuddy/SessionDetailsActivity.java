package com.example.studybuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.adapter.ParticipantsAdapter;
import com.example.studybuddy.model.Session;
import com.example.studybuddy.model.Student;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SessionDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvInfo;
    private Button btnJoinLeave;

    private Session session;
    private ParticipantsAdapter participantsAdapter;
    private boolean joined = false;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvInfo = findViewById(R.id.tvInfo);
        btnJoinLeave = findViewById(R.id.btnJoinLeave);
        Button btnBack = findViewById(R.id.btnBack);
        RecyclerView rvParticipants = findViewById(R.id.rvParticipants);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        session = (Session) getIntent().getSerializableExtra("session");

        btnBack.setOnClickListener(v -> finish());

        if (session != null) {
            tvTitle.setText(session.getCourseName());
            tvInfo.setText(
                    session.getTopic() + "\n" +
                            session.getTime() + "\n" +
                            session.getLocation() + "\n" +
                            "GPS: " + session.getLatitude() + ", " + session.getLongitude()
            );

            joined = isCurrentUserParticipant();
            btnJoinLeave.setText(joined ? "Leave Session" : "Join Session");

            participantsAdapter = new ParticipantsAdapter(session.getParticipants());
            rvParticipants.setLayoutManager(new LinearLayoutManager(this));
            rvParticipants.setAdapter(participantsAdapter);

            Bundle bundle = new Bundle();
            bundle.putString("course_name", session.getCourseName());
            analytics.logEvent("session_opened", bundle);
        }

        btnJoinLeave.setOnClickListener(v -> {
            if (session == null) return;

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (session.getDocumentId() == null || session.getDocumentId().isEmpty()) {
                Toast.makeText(this, "Cannot update this session without a Firestore ID.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!joined) {
                if (session.getParticipants().size() >= session.getMaxParticipants()) {
                    Toast.makeText(this, "This session is full.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String displayName = user.getDisplayName() == null ? "Signed-in Student" : user.getDisplayName();
                String email = user.getEmail() == null ? "Google Account" : user.getEmail();
                session.getParticipants().add(new Student(displayName, email, user.getUid()));
                joined = true;
                btnJoinLeave.setText("Leave Session");
            } else {
                ArrayList<Student> participants = session.getParticipants();
                for (int i = 0; i < participants.size(); i++) {
                    if (user.getUid().equals(participants.get(i).getUid())) {
                        participants.remove(i);
                        break;
                    }
                }
                joined = false;
                btnJoinLeave.setText("Join Session");
            }

            saveParticipants();
        });
    }

    private boolean isCurrentUserParticipant() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || session == null) return false;

        for (Student student : session.getParticipants()) {
            if (user.getUid().equals(student.getUid())) {
                return true;
            }
        }

        return false;
    }

    private void saveParticipants() {
        db.collection("Sessions")
                .document(session.getDocumentId())
                .update("participants", session.getParticipants())
                .addOnSuccessListener(unused -> {
                    participantsAdapter.notifyDataSetChanged();

                    Bundle bundle = new Bundle();
                    bundle.putString("course_name", session.getCourseName());
                    bundle.putString("action", joined ? "join" : "leave");
                    analytics.logEvent("session_participation_changed", bundle);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
