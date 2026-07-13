package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Invisible entry point: routes to Login when no Firebase user is signed
 * in, otherwise straight to the sessions list.
 */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Class<?> nextActivity = FirebaseAuth.getInstance().getCurrentUser() == null
                ? LoginActivity.class
                : MainActivity.class;

        startActivity(new Intent(this, nextActivity));
        finish();
    }
}
