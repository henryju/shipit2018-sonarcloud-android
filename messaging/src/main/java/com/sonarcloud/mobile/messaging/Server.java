package com.sonarcloud.mobile.messaging;

import com.google.firebase.messaging.FirebaseMessagingException;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.util.*;

public class Server extends NanoHTTPD {
  private static final int PORT = 6666;

  private final Messaging messaging;

  Server(Messaging messaging) {
    super(PORT);
    this.messaging = messaging;
  }

  @Override
  public void start() throws IOException {
    super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("Server started\n");
  }

  @Override
  public Response serve(IHTTPSession session) {
    Method method = session.getMethod();
    String uri = session.getUri();

    System.out.println("Call: " + session.getUri());

    try {

      if (method.equals(Method.POST)) {
        if ("/register".equals(uri)) {
          return handleRegistration(readBody(session));
        } else if ("/webhook".equals(uri)) {
          return handleWebhook(readBody(session));
        }
      }
    } catch (IOException | FirebaseMessagingException e) {
      e.printStackTrace();
      return badRequest("Internal error");
    }

    return badRequest("Invalid uri/method");
  }

  private static String readBody(IHTTPSession session) throws IOException {
    // don't close this, otherwise the connection will be closed
    InputStream stream = session.getInputStream();
    int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
    byte[] b = new byte[contentLength];
    // FIXME
    stream.read(b, 0, contentLength);
    return new String(b);
  }

  private Response handleRegistration(String body) {
    System.out.println("REGISTRATION: " + body);

    String[] lines = body.split("\n");
    if (lines.length != 2) {
      return badRequest("Invalid format");
    }

    String uuid = lines[0];
    String[] projectKeys = lines[1].split(",");

    if (projectKeys.length == 0) {
      return badRequest("Invalid format");
    }

    messaging.register(uuid, new HashSet<>(Arrays.asList(projectKeys)));
    return ok();
  }

  private Response handleWebhook(String json) throws FirebaseMessagingException {
    System.out.println("WEBHOOK: " + json);
    Webhook webhook = Webhook.parse(json);
    messaging.process(webhook.project.key, json);
    return ok();
  }

  private static Response ok() {
    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK");
  }

   private static Response badRequest(String reason) {
    System.out.println("Bad request: " + reason + "\n");
    return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_HTML, reason);
  }
}
