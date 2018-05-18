package com.example.laurawacrenier.sonarcloud_for_android;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * See https://firebase.google.com/docs/cloud-messaging/android/receive#sample-receive.
 */
public class NotificationHandler extends FirebaseMessagingService {
    private static final String TAG = "Notif";

    public NotificationHandler() {
        super();
        Log.d(TAG, "Starting Message service");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }
}