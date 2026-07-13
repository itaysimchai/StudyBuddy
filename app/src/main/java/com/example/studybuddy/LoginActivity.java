package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Login screen offering two authentication methods:
 * 1. Google Sign-In (required by the project spec)
 * 2. Email/password sign-in and registration (bonus method)
 * Both end up as Firebase Authentication users and continue to MainActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private TextView tvLoginStatus;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        tvLoginStatus = findViewById(R.id.tvLoginStatus);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        Button btnEmailSignIn = findViewById(R.id.btnEmailSignIn);
        Button btnEmailRegister = findViewById(R.id.btnEmailRegister);

        // The web client id is generated into google-services.json once the
        // Google provider is enabled in the Firebase console. Look it up
        // dynamically so the app still builds if it is missing.
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
        btnEmailSignIn.setOnClickListener(v -> signInWithEmail());
        btnEmailRegister.setOnClickListener(v -> registerWithEmail());
    }

    /** Exchanges the Google account's ID token for a Firebase session. */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        if (account.getIdToken() == null) {
            Toast.makeText(this, "Missing Google ID token. Update Firebase OAuth configuration.", Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnSuccessListener(authResult -> onLoginSuccess("google"))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /** Bonus auth method: signs in an existing email/password user. */
    private void signInWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (!validateEmailInput(email, password)) return;

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> onLoginSuccess("email"))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /** Bonus auth method: creates a new email/password account and signs in. */
    private void registerWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (!validateEmailInput(email, password)) return;

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "email");
                    analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
                    goToMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private boolean validateEmailInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter both email and password.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Logs the login analytics event and moves on to the sessions list. */
    private void onLoginSuccess(String method) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        goToMain();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
