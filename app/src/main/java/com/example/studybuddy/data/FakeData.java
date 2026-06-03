package com.example.studybuddy.data;

import com.example.studybuddy.model.Session;
import com.example.studybuddy.model.Student;

import java.util.ArrayList;

public class FakeData {

    public static ArrayList<Session> createSessions() {

        ArrayList<Session> sessions = new ArrayList<>();

        // Session 1 Participants
        ArrayList<Student> participants1 = new ArrayList<>();
        participants1.add(new Student("Daniel Cohen", "Computer Science"));
        participants1.add(new Student("Maya Levi", "Business Administration"));

        // Session 2 Participants
        ArrayList<Student> participants2 = new ArrayList<>();
        participants2.add(new Student("Sarah Levi", "Data Science"));

        // Session 3 Participants
        ArrayList<Student> participants3 = new ArrayList<>();
        participants3.add(new Student("Amit Ben David", "Information Systems"));

        // Session 4 Participants
        ArrayList<Student> participants4 = new ArrayList<>();
        participants4.add(new Student("Noa Friedman", "Computer Science"));
        participants4.add(new Student("Itay Simchai", "Business Administration"));

        sessions.add(new Session(
                "Calculus 1",
                "Calculus Exam Preparation",
                "2026-06-10 18:00",
                "Reichman Library",
                10,
                participants1
        ));

        sessions.add(new Session(
                "Introduction to Data Science",
                "Data Science Workshop",
                "2026-06-12 17:00",
                "Zoom",
                15,
                participants2
        ));

        sessions.add(new Session(
                "Database Systems",
                "Database Systems Review",
                "2026-06-14 19:00",
                "Building A Room 203",
                12,
                participants3
        ));

        sessions.add(new Session(
                "Mobile App Development",
                "Android Development Study Group",
                "2026-06-16 16:30",
                "Computer Lab",
                20,
                participants4
        ));

        return sessions;
    }
}