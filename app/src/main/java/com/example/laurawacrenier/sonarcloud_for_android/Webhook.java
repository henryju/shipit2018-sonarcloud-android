package com.example.laurawacrenier.sonarcloud_for_android;

import com.google.gson.Gson;

public class Webhook {
  private static Gson gson = new Gson();

  Project project;
  QualityGate qualityGate;

  static class Project {
    String key;
    String name;
    String url;
  }

  static class QualityGate {
    String name;
    String status;
  }

  public static Webhook parse(String json) {
    return gson.fromJson(json, Webhook.class);
  }
}
