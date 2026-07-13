package com.example.studybuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.adapter.SessionsAdapter;
import com.example.studybuddy.model.Session;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Lists all sessions from Firestore sorted by distance from the user's
 * current GPS location (phone-capability requirement). Sessions without
 * coordinates are pushed to the end of the list.
 */
public class NearbySessionsActivity extends AppCompatActivity {

    private TextView tvNearbyStatus;
    private SessionsAdapter adapter;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadNearbySessions();
                } else {
                    Toast.makeText(this, "Location permission is needed for nearby sessions.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_sessions);

        db = FirebaseFirestore.getInstance();
        tvNearbyStatus = findViewById(R.id.tvNearbyStatus);
        RecyclerView rvNearbySessions = findViewById(R.id.rvNearbySessions);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnRefreshNearby = findViewById(R.id.btnRefreshNearby);

        adapter = new SessionsAdapter(this, new ArrayList<>());
        rvNearbySessions.setLayoutManager(new LinearLayoutManager(this));
        rvNearbySessions.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnRefreshNearby.setOnClickListener(v -> requestOrLoadNearbySessions());

        requestOrLoadNearbySessions();
    }

    private void requestOrLoadNearbySessions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadNearbySessions();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadNearbySessions() {
        Location userLocation = getBestLastKnownLocation();
        if (userLocation == null) {
            tvNearbyStatus.setText("No current location available yet.");
            return;
        }

        tvNearbyStatus.setText("Sorting sessions by distance from your current location.");

        db.collection("Sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Session> sessions = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Session session = document.toObject(Session.class);
                        session.setDocumentId(document.getId());

                        if (session.getLatitude() != 0 || session.getLongitude() != 0) {
                            session.setDistanceKm(distanceKm(
                                    userLocation.getLatitude(),
                                    userLocation.getLongitude(),
                                    session.getLatitude(),
                                    session.getLongitude()
                            ));
                        } else {
                            session.setDistanceKm(Double.MAX_VALUE);
                        }

                        sessions.add(session);
                    }

                    Collections.sort(sessions, (first, second) ->
                            Double.compare(first.getDistanceKm(), second.getDistanceKm())
                    );

                    adapter.updateList(sessions);
                    if (sessions.isEmpty()) {
                        tvNearbyStatus.setText("No sessions found in Firestore.");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
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

    private double distanceKm(double startLat, double startLng, double endLat, double endLng) {
        float[] result = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, result);
        return result[0] / 1000.0;
    }
}
