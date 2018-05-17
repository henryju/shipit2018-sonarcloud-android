package com.example.laurawacrenier.sonarcloud_for_android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MyProjectRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mCookie = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCookie = data.getStringExtra("cookie");

        new LoadFavorites().execute();

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void login() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new LoadFavorites().execute();

        // specify an adapter (see also next example)
        mAdapter = new MyProjectRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    class LoadFavorites extends AsyncTask<Void, Void, Favorites> {

        protected Favorites doInBackground(Void... noparams) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            final Request original = chain.request();

                            final Request authorized = original.newBuilder()
                                    .addHeader("Cookie", "JWT-SESSION=eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJBV051bUNpNUNpVjY3eS14TmxhRyIsInN1YiI6InZhbGhyaXN0b3ZAZ2l0aHViIiwiaWF0IjoxNTI2NTY4ODU1LCJleHAiOjE1MjY4MjgwNTUsImxhc3RSZWZyZXNoVGltZSI6MTUyNjU2ODg1NTczNywieHNyZlRva2VuIjoiNDF2dmkyMGcxNHQ1Z2d0ZGI4YjdqczFzdDkifQ.e2QWXNALTtOwr54Qkaes-_1TximpQ3yOPOPrWehmcQw")
                                    .build();

                            return chain.proceed(authorized);
                        }
                    }).build();
            String credential = Credentials.basic("ea97c37ca106a3a5921a9b79c937d24b29a2e8bd", "");
            Request request = new Request.Builder()
                    .url("https://sonarcloud.io/api/favorites/search")
                    //.addHeader("Authorization", credential)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return new Gson().fromJson(response.body().charStream(), Favorites.class);
                } else {
                    Log.e("SonarCloud", "Unable to fetch favorite projects: " + response.code());
                }
            } catch (Exception e) {
                Log.e("SonarCloud", "Unable to fetch favorite projects: " + e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Favorites favs) {
            mAdapter.getValues().clear();
            if (favs != null) {
                mAdapter.getValues().addAll(favs.favorites.stream().map(f -> new Project(f.key, f.name)).collect(Collectors.toList()));
            }
            mAdapter.notifyDataSetChanged();
        }
    }


    static class Favorites {

        List<Favorite> favorites;

    }

    static class Favorite {
        String key;
        String name;
    }
}
