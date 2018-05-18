package com.sonarcloud.mobile.messaging;

import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    Messaging messaging = new Messaging();
    messaging.init();
    Server server = new Server(messaging);
    server.start();
  }
}
