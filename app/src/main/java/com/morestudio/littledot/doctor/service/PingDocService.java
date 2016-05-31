package com.morestudio.littledot.doctor.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User 3 on 31.5.2016.
 */
public class PingDocService extends Service {

    private static final String API_URL = "http://littledot.flipkod.com/api/doctor/ping";
    private Timer timer;
    String token;

    @Override
    public void onCreate() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("token", "");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                new PingDoc().execute();
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 1000, 180000);


        return START_STICKY;
    }

    public class PingDoc extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpPost request = new HttpPost(API_URL);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            request.addHeader("Authorization", token);


            HttpClient httpClient = new DefaultHttpClient();

            try {
                HttpResponse response = httpClient.execute(request);
                Log.d("Login: Response", EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                Log.d("Error", String.valueOf(e));
            }

            return null;
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
