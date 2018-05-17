package com.example.laurawacrenier.sonarcloud_for_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final WebView web = findViewById(R.id.web);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("sonarcloud.io") && !url.contains("sonarcloud.io/sessions/new")) {
                    String cookiesHeader = CookieManager.getInstance().getCookie(url);
                    if (cookiesHeader != null) {
                        Pattern p = Pattern.compile(".*(JWT-SESSION=.*)\\;?");
                        Matcher m = p.matcher(cookiesHeader);
                        if (m.matches()) {
                            String cookie = m.group(1);

                            Intent data = new Intent();
                            data.putExtra("cookie", cookie);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
                }
                super.onPageFinished(view, url);
            }
        });
        web.loadUrl("https://www.sonarcloud.io/sessions/new");
    }
}
