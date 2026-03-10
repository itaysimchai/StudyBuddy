package com.example.studybuddy.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Session implements Serializable {

    private String courseName;
    private String topic;
    private String time;
    private String location;
    private int maxParticipants;
    private ArrayList<Student> participants;

    public Session(String courseName, String topic, String time, String location,
                   int maxParticipants, ArrayList<Student> participants) {
        this.courseName = courseName;
        this.topic = topic;
        this.time = time;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.participants = participants;
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

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public ArrayList<Student> getParticipants() {
        return participants;
    }
}