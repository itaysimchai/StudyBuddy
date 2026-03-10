package com.example.studybuddy.model;

import java.io.Serializable;

public class Student implements Serializable {

    private String name;
    private String major;

    public Student(String name, String major) {
        this.name = name;
        this.major = major;
    }

    public String getName() {
        return name;
    }

    public String getMajor() {
        return major;
    }
}