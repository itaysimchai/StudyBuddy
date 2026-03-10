package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Button btnNavHome, btnNavProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavProfile = findViewById(R.id.btnNavProfile);

        btnNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnNavProfile.setOnClickListener(v -> {
            // already here
        });
    }
}