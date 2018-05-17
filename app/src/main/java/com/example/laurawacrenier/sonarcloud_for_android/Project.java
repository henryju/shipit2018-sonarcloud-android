package com.example.laurawacrenier.sonarcloud_for_android;

public class Project {

    public final String id;
    public final String name;
    public final String alert_status;
    public final String bugs;
    public final String vulnerabilities;
    public final String codeSmells;

    public Project(String id, String name, String alert_status, String bugs, String vulnerabilities, String codeSmells) {
        this.id = id;
        this.name = name;
        this.alert_status = alert_status;
        this.bugs = bugs;
        this.vulnerabilities = vulnerabilities;
        this.codeSmells = codeSmells;
    }

    @Override
    public String toString() {
        return name;
    }
}
