package com.example.studybuddy.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;

public class Session implements Serializable {

    private String documentId;
    private String courseName;
    private String topic;
    private String time;
    private String location;
    private double latitude;
    private double longitude;
    private int maxParticipants;
    private ArrayList<Student> participants;
    private double distanceKm;

    public Session() {
        participants = new ArrayList<>();
    }

    public Session(String courseName, String topic, String time, String location,
                   double latitude, double longitude, int maxParticipants,
                   ArrayList<Student> participants) {
        this.courseName = courseName;
        this.topic = topic;
        this.time = time;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxParticipants = maxParticipants;
        this.participants = participants;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTopic() {
        return topic;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public ArrayList<Student> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        return participants;
    }

    @Exclude
    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setParticipants(ArrayList<Student> participants) {
        this.participants = participants;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
