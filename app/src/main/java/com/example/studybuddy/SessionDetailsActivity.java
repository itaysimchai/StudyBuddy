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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Shows the full details of a study session and lets the signed-in user
 * join or leave it. The participants list is updated inside a Firestore
 * transaction so two users acting at the same time cannot overwrite
 * each other's changes.
 */
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

        btnJoinLeave.setOnClickListener(v -> toggleParticipation());
    }

    /** Returns true when the signed-in user already appears in the participants list. */
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

    /**
     * Joins or leaves the session inside a Firestore transaction: the
     * participants list is re-read from the database, modified, and written
     * back atomically, so a stale local copy can never erase other users.
     */
    private void toggleParticipation() {
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

        boolean joining = !joined;
        DocumentReference sessionRef = db.collection("Sessions").document(session.getDocumentId());

        db.runTransaction(transaction -> {
            Session freshSession = transaction.get(sessionRef).toObject(Session.class);
            if (freshSession == null) {
                throw new IllegalStateException("Session no longer exists.");
            }

            ArrayList<Student> participants = freshSession.getParticipants();

            // Remove the user first so joining twice can never duplicate them.
            for (int i = participants.size() - 1; i >= 0; i--) {
                if (user.getUid().equals(participants.get(i).getUid())) {
                    participants.remove(i);
                }
            }

            if (joining) {
                if (participants.size() >= freshSession.getMaxParticipants()) {
                    throw new IllegalStateException("This session is full.");
                }
                // Email/password users have no display name, so fall back to
                // the part of their email before the @.
                String email = user.getEmail() == null ? "Unknown Email" : user.getEmail();
                String displayName = user.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Student";
                }
                participants.add(new Student(displayName, email, user.getUid()));
            }

            transaction.update(sessionRef, "participants", participants);
            return participants;
        }).addOnSuccessListener(updatedParticipants -> {
            joined = joining;
            btnJoinLeave.setText(joined ? "Leave Session" : "Join Session");

            // Refresh the local copy and the list on screen with the real data.
            session.setParticipants(new ArrayList<>(updatedParticipants));
            participantsAdapter.updateList(session.getParticipants());

            Bundle bundle = new Bundle();
            bundle.putString("course_name", session.getCourseName());
            bundle.putString("action", joined ? "join" : "leave");
            analytics.logEvent("session_participation_changed", bundle);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }
}
