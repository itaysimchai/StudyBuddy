package com.example.studybuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studybuddy.model.Session;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Form for creating a new study session. Uses the phone's GPS
 * (last known location) to attach real coordinates to the session
 * before saving it to Firestore.
 */
public class CreateSessionActivity extends AppCompatActivity {

    private EditText etCourseName, etTopic, etTime, etLocationName, etMaxParticipants;
    private TextView tvGpsStatus;
    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private double latitude = 0;
    private double longitude = 0;
    private boolean hasGpsLocation = false;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fillCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission is required to attach GPS.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);

        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        etCourseName = findViewById(R.id.etCourseName);
        etTopic = findViewById(R.id.etTopic);
        etTime = findViewById(R.id.etTime);
        etLocationName = findViewById(R.id.etLocationName);
        etMaxParticipants = findViewById(R.id.etMaxParticipants);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        Button btnUseGps = findViewById(R.id.btnUseGps);
        Button btnSaveSession = findViewById(R.id.btnSaveSession);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnUseGps.setOnClickListener(v -> requestOrFillLocation());
        btnSaveSession.setOnClickListener(v -> saveSession());
    }

    private void requestOrFillLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fillCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fillCurrentLocation() {
        Location location = getBestLastKnownLocation();
        if (location == null) {
            Toast.makeText(this, "No current location available yet. Try again near a window or outside.", Toast.LENGTH_LONG).show();
            return;
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        hasGpsLocation = true;
        tvGpsStatus.setText("GPS attached: " + latitude + ", " + longitude);
    }

    private Location getBestLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location network = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps == null) return network;
        if (network == null) return gps;
        return gps.getTime() > network.getTime() ? gps : network;
    }

    private void saveSession() {
        String courseName = etCourseName.getText().toString().trim();
        String topic = etTopic.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String locationName = etLocationName.getText().toString().trim();
        String maxText = etMaxParticipants.getText().toString().trim();

        if (courseName.isEmpty() || topic.isEmpty() || time.isEmpty() ||
                locationName.isEmpty() || maxText.isEmpty()) {
            Toast.makeText(this, "Fill all fields before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasGpsLocation) {
            Toast.makeText(this, "Attach GPS location before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxParticipants;
        try {
            maxParticipants = Integer.parseInt(maxText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Max participants must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        Session session = new Session(
                courseName,
                topic,
                time,
                locationName,
                latitude,
                longitude,
                maxParticipants,
                new ArrayList<>()
        );

        db.collection("Sessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("course_name", courseName);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        bundle.putString("creator_uid", auth.getCurrentUser().getUid());
                    }
                    analytics.logEvent("session_created", bundle);

                    Toast.makeText(this, "Session created.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
