package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Shows the signed-in Firebase user's name and email, plus study
 * preferences that are stored per-user in the Firestore "Users"
 * collection (no static data). Logout signs out of both Firebase
 * and Google before returning to Login.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView tvProfileName, tvProfileEmail;
    private CheckBox cbMorning, cbLibrary, cbSmallGroups;

    // True while the checkboxes are being set from Firestore, so the
    // change listeners don't write the loaded values straight back.
    private boolean loadingPreferences = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        cbMorning = findViewById(R.id.cbMorning);
        cbLibrary = findViewById(R.id.cbLibrary);
        cbSmallGroups = findViewById(R.id.cbSmallGroups);
        Button btnNavHome = findViewById(R.id.btnNavHome);
        Button btnNavProfile = findViewById(R.id.btnNavProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvProfileName.setText("Name: " + safeText(user.getDisplayName(), "Signed-in Student"));
            tvProfileEmail.setText("Email: " + safeText(user.getEmail(), "Google Account"));
            loadPreferences(user.getUid());
        }

        cbMorning.setOnCheckedChangeListener((v, checked) -> savePreferences());
        cbLibrary.setOnCheckedChangeListener((v, checked) -> savePreferences());
        cbSmallGroups.setOnCheckedChangeListener((v, checked) -> savePreferences());

        btnNavHome.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MainActivity.class))
        );

        btnNavProfile.setOnClickListener(v -> {
            // already here
        });

        btnLogout.setOnClickListener(v -> {
            GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, options);

            auth.signOut();
            googleSignInClient.signOut();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /** Reads this user's saved preferences from Firestore into the checkboxes. */
    private void loadPreferences(String uid) {
        db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        loadingPreferences = true;
                        cbMorning.setChecked(Boolean.TRUE.equals(document.getBoolean("prefMorning")));
                        cbLibrary.setChecked(Boolean.TRUE.equals(document.getBoolean("prefLibrary")));
                        cbSmallGroups.setChecked(Boolean.TRUE.equals(document.getBoolean("prefSmallGroups")));
                        loadingPreferences = false;
                    } else {
                        // First visit: store the defaults so the document exists.
                        savePreferences();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not load preferences: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /** Writes the current checkbox states to the user's Firestore document. */
    private void savePreferences() {
        if (loadingPreferences) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("email", user.getEmail());
        prefs.put("prefMorning", cbMorning.isChecked());
        prefs.put("prefLibrary", cbLibrary.isChecked());
        prefs.put("prefSmallGroups", cbSmallGroups.isChecked());

        DocumentReference userDoc = db.collection("Users").document(user.getUid());
        userDoc.set(prefs, SetOptions.merge())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not save preferences: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
