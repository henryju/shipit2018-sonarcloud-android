package com.example.laurawacrenier.sonarcloud_for_android;

import java.util.Map;

public class Project {

    public final String id;
    public final String name;
    public final Map<String, String> measures;

    public Project(String id, String name, Map<String, String> measures) {
        this.id = id;
        this.name = name;
        this.measures = measures;
    }

    @Override
    public String toString() {
        return name;
    }
}
