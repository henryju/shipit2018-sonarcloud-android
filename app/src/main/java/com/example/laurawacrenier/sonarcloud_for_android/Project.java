package com.example.laurawacrenier.sonarcloud_for_android;

public class Project {

    public final String id;
    public final String name;
    public final String alert_status;

    public Project(String id, String name, String alert_status) {
        this.id = id;
        this.name = name;
        this.alert_status = alert_status;
    }

    @Override
    public String toString() {
        return name;
    }
}
