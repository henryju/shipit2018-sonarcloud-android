package com.example.laurawacrenier.sonarcloud_for_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private MyProjectRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String cookie = data.getStringExtra("cookie");
        storeCookie(cookie);
        new LoadDataFromSonarCloud(cookie).execute();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void storeCookie(String cookie) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.auth_cookie), cookie);
        editor.apply();
    }

    private String readCookie() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.auth_cookie), null);
    }

    protected void login() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onRefresh() {
        reloadData();
    }

    private void reloadData() {
        String cookie = readCookie();
        if (cookie == null) {
            login();
        } else {
            new LoadDataFromSonarCloud(cookie).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyProjectRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        new RegisterFirebase().execute("project1", "project2");
    }


    private class RegisterFirebase extends AsyncTask<String, String, Void> {
        private NotificationRegistration notificationRegistration = new NotificationRegistration();

        @Override
        protected Void doInBackground(String... projectKeys) {
            Set<String> projectSet = new HashSet<>(Arrays.asList(projectKeys));
            try {
                notificationRegistration.register(projectSet);
            } catch (IOException e) {
                Log.w("Registration", "Failed to register projects for notifications", e);
            }
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
    }

    class LoadDataFromSonarCloud extends AsyncTask<Void, Void, List<Project>> {

        private final String cookie;

        private LoadDataFromSonarCloud(String cookie) {
            this.cookie = cookie;
        }

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        protected List<Project> doInBackground(Void... noparams) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            final Request original = chain.request();

                            final Request authorized = original.newBuilder()
                                    .addHeader("Cookie", cookie)
                                    .build();

                            return chain.proceed(authorized);
                        }
                    }).build();
            Log.i("SonarCloud", "Loading favorites projects");
            Request request = new Request.Builder()
                    .url("https://sonarcloud.io/api/favorites/search")
                    .build();

            Map<String, String> projectKeyNamePair;
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    projectKeyNamePair = new Gson().fromJson(response.body().charStream(), Favorites.class).favorites.stream().collect(Collectors.toMap(f -> f.key, f -> f.name));
                } else if (response.code() == 401) {
                    MainActivity.this.login();
                    return null;
                } else {
                    Log.e("SonarCloud", "Unable to fetch favorite projects: " + response.code());
                    return null;
                }
            } catch (Exception e) {
                Log.e("SonarCloud", "Unable to fetch favorite projects: " + e.getMessage());
                return null;
            }

            String url = "https://sonarcloud.io/api/measures/search?metricKeys=alert_status%2Cbugs%2Creliability_rating%2Cvulnerabilities%2Csecurity_rating%2Ccode_smells%2Csqale_rating%2Cduplicated_lines_density%2Ccoverage%2Cncloc%2Cncloc_language_distribution&projectKeys=" + projectKeyNamePair.keySet()
                    .stream()
                    .map(this::urlEncode)
                    .collect(Collectors.joining(","));
            Request requestMetric = new Request.Builder()
                    .url(url)
                    .build();
            Log.i("SonarCloud", "Loading measures");
            Map<String, Map<String, String>> measuresByComponentKey = new HashMap<>();
            try (Response response = client.newCall(requestMetric).execute()) {
                if (response.isSuccessful()) {
                    new Gson().fromJson(response.body().charStream(), Measures.class).measures.forEach(m -> {
                        measuresByComponentKey.computeIfAbsent(m.component, k -> new HashMap<>()).put(m.metric, m.value);
                    });
                } else if (response.code() == 401) {
                    MainActivity.this.login();
                    return null;
                } else {
                    Log.e("SonarCloud", "Unable to fetch measures: " + response.code());
                    return null;
                }
            } catch (Exception e) {
                Log.e("SonarCloud", "Unable to fetch measures: " + e.getMessage());
                return null;
            }
            return projectKeyNamePair.entrySet().stream().filter(e -> measuresByComponentKey.containsKey(e.getKey()))
                    .map(e -> new Project(e.getKey(), e.getValue(),
                            measuresByComponentKey.get(e.getKey())))
                    .collect(Collectors.toList());

        }

        private String urlEncode(String s) {
            try {
                return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        protected void onPostExecute(List<Project> projects) {
            mAdapter.getValues().clear();
            if (projects != null) {
                mAdapter.getValues().addAll(projects);
            }
            mAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    static class Favorites {
        List<Favorite> favorites;
    }

    static class Measures {
        List<Measure> measures;
    }

    static class Favorite {
        String key;
        String name;
    }

    static class Measure {
        String metric;
        String value;
        String component;
    }
}
