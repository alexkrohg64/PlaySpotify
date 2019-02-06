package com.krohgsolutions.playspotify;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

class PerformSearchTask extends AsyncTask<String, Integer, String> {
    protected String doInBackground(String... urls) {
        try {
            Log.d("MainActivity","Executing Search for Random Track");
            URL url = new URL("https://api.spotify.com/v1/search/?q=name%3Agold%26type=track%26market=from_token");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", MainActivity.getAuthToken());
            connection.setRequestMethod("GET");
            Log.d("MainActivity", connection.getResponseCode() + "");
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = "";
            String line = null;
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }

            return content;

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e("MainActivity", sw.toString());

            return "";
        }
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        return;
    }
}
