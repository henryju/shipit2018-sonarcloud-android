package com.sonarcloud.mobile.messaging;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Messaging {
  private static final String DATABASE = "https://sonarcloud-3de8d.firebaseio.com";
  private static final String ACCOUNT_INFO = "serviceAccountKey.json";

  private Map<String, Set<String>> projectsPerUser = new HashMap<>();

  public void init() throws IOException {
    InputStream serviceAccountStream = Messaging.class.getClassLoader().getResourceAsStream(ACCOUNT_INFO);
    FirebaseOptions options = new FirebaseOptions.Builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
      .setDatabaseUrl(DATABASE)
      .build();

    FirebaseApp.initializeApp(options);
  }

  public void process(String projectKey, String qualityGate, String webhook) throws FirebaseMessagingException {
    //TODO optimize this O(N.M)

    Set<String> usersToNotify = new HashSet<>();

    for (Map.Entry<String, Set<String>> e : projectsPerUser.entrySet()) {
      if (e.getValue().contains(projectKey)) {
        usersToNotify.add(e.getKey());
      }
    }

    for (String token : usersToNotify) {
      sendMessage(projectKey, qualityGate, token, webhook);
    }
  }

  public void register(String uuid, Set<String> projectKeys) {
    projectsPerUser.put(uuid, projectKeys);
    System.out.println(String.format("Registered for user %s the following projects: %s", uuid, Arrays.toString(projectKeys.toArray(new String[projectKeys.size()]))));
  }

  private void sendMessage(String projectKey, String qualityGate, String token, String webhook) throws FirebaseMessagingException {
    System.out.println(String.format("Sending message to '%s' ", token));

    // See documentation on defining a message payload.
    String msg = "Quality gate of " + projectKey + " is now " + qualityGate;
    Message message = Message.builder()
      .setNotification(new Notification(msg, msg))
      .putData("webhook", webhook)
      .setToken(token)
      .build();

    String response = FirebaseMessaging.getInstance().send(message);

    // Response is a message ID string.
    System.out.println("Successfully sent message: " + response);
  }
}
