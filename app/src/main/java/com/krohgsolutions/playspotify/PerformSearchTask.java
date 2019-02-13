package com.krohgsolutions.playspotify;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

class PerformSearchTask extends AsyncTask<String, Integer, String> {
    protected String doInBackground(String... urls) {
        try {
            Log.d("PerformSearchTask","Executing Search for Random Track");
            Random r = new Random();
            int offset = r.nextInt(9950);
            int randomCharOffset = r.nextInt(26);
            //add ASCII
            char randomChar = (char)(randomCharOffset + 97);
            Log.d("PerformSearchTask", "xi = " + randomCharOffset + " , x = " + randomChar);

            URL url = new URL("https://api.spotify.com/v1/search/?q=" + randomChar + "*&type=track&market=from_token&limit=50&offset=" + offset);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", "Bearer " + MainActivity.getAuthToken());
            connection.setRequestMethod("GET");
            Log.d("PerformSearchTask", connection.getResponseCode() + "");
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = "";
            String line = null;
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }

            int resultIndex = r.nextInt(50);

            for (int i = 0; i <= resultIndex; i++) {
                content = content.substring(content.indexOf("spotify:track"));
            }

            String uri = content.substring(0, content.indexOf("\""));

            MainActivity.addSpotifyURIToQueue(uri);

            return uri;

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
