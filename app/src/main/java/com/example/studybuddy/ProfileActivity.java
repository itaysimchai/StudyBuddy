package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView tvProfileName, tvProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        Button btnNavHome = findViewById(R.id.btnNavHome);
        Button btnNavProfile = findViewById(R.id.btnNavProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvProfileName.setText("Name: " + safeText(user.getDisplayName(), "Signed-in Student"));
            tvProfileEmail.setText("Email: " + safeText(user.getEmail(), "Google Account"));
        }

        btnNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

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

    private String safeText(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
