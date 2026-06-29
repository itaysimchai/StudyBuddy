package com.example.studybuddy.model;

import java.io.Serializable;

public class Student implements Serializable {

    private String name;
    private String major;
    private String uid;

    public Student() {
    }

    public Student(String name, String major) {
        this.name = name;
        this.major = major;
    }

    public Student(String name, String major, String uid) {
        this.name = name;
        this.major = major;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getMajor() {
        return major;
    }

    public String getUid() {
        return uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
