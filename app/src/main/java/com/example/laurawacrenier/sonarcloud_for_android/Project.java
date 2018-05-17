package com.example.laurawacrenier.sonarcloud_for_android;

public class Project {

    public final String id;
    public final String name;

    public Project(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
