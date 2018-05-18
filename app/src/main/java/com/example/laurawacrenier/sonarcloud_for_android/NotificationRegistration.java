package com.example.laurawacrenier.sonarcloud_for_android;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationRegistration {
    private static final String TAG = "Registration";
    private static final String endpoint = "http://35.180.63.162:6666";

    /**
     * Attempt to register a set of project keys to receive notifications about their quality gates.
     *
     * @return whether the operation succeeded.
     */
    public void register(Set<String> projectKeys) throws IOException {
        Log.i(TAG, "Registering " + projectKeys.size() + " projects for notifications");

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token == null) {
            Log.e(TAG, "Failed to get the Firebase token");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String contents = getContent(token, projectKeys);
        Log.i(TAG, "Sending: " + contents);
        Request request = new Request.Builder()
                .url(endpoint + "/register")
                .post(RequestBody.create(MediaType.parse("text/plain"), contents))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.i(TAG, "Registered: " + contents);
            } else {
                Log.e(TAG, "Fail to register to '" + endpoint + "': " + response.code() + " " + response.body().string());
            }
        }
    }

    private String getContent(String token, Set<String> projectKeys) {
        String projects = TextUtils.join(",", projectKeys);
        return token + "\n" + projects;
    }
}
