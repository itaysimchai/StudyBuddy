package com.example.studybuddy.data;

import com.example.studybuddy.model.Session;
import com.example.studybuddy.model.Student;

import java.util.ArrayList;

public class FakeData {

    public static ArrayList<Session> createSessions() {
        ArrayList<Session> sessions = new ArrayList<>();

        ArrayList<Student> participants1 = new ArrayList<>();
        participants1.add(new Student("Alex", "Computer Science"));
        participants1.add(new Student("Maya", "Business"));

        ArrayList<Student> participants2 = new ArrayList<>();
        participants2.add(new Student("Noa", "Psychology"));

        ArrayList<Student> participants3 = new ArrayList<>();

        sessions.add(new Session(
                "CS Algorithms",
                "Dynamic Programming Review",
                "Today 16:00",
                "Library Room 204",
                10,
                participants1
        ));

        sessions.add(new Session(
                "Advanced Calculus",
                "Midterm Practice",
                "Tomorrow 14:00",
                "Library Floor 2",
                8,
                participants2
        ));

        sessions.add(new Session(
                "English Writing",
                "Essay Structure",
                "Thursday 11:00",
                "Cafeteria",
                6,
                participants3
        ));

        return sessions;
    }
}