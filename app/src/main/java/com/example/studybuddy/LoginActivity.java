package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private TextView tvLoginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        tvLoginStatus = findViewById(R.id.tvLoginStatus);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        int webClientId = getResources().getIdentifier(
                "default_web_client_id",
                "string",
                getPackageName()
        );

        if (webClientId == 0) {
            tvLoginStatus.setText("Google sign-in needs an updated google-services.json with an OAuth web client.");
        }

        GoogleSignInOptions.Builder optionsBuilder =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail();

        if (webClientId != 0) {
            optionsBuilder.requestIdToken(getString(webClientId));
        }

        googleSignInClient = GoogleSignIn.getClient(this, optionsBuilder.build());

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        GoogleSignInAccount account = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        btnGoogleSignIn.setOnClickListener(v -> signInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        if (account.getIdToken() == null) {
            Toast.makeText(this, "Missing Google ID token. Update Firebase OAuth configuration.", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnSuccessListener(authResult -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "google");
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
