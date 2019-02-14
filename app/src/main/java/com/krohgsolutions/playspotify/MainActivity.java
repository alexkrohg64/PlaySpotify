package com.krohgsolutions.playspotify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.AuthenticationClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "fee2c0b9aebc4e4a838507645af61f25";
    private static final String REDIRECT_URI = "spotify-app-remote://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;
    private static String AUTH_TOKEN;
    private static List<String> spotifyURIs = new ArrayList<>();
    private static String CURRENT_TITLE;
    private static String CURRENT_ARTIST;
    private static String CURRENT_ALBUM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void initializePlayer() {
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected to Player");

                        // Subscribe to PlayerState
                        mSpotifyAppRemote.getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(new Subscription.EventCallback<PlayerState>() {
                                    @Override
                                    public void onEvent(PlayerState playerState) {
                                        final Track track = playerState.track;
                                        if (track != null && !track.name.equalsIgnoreCase(CURRENT_TITLE)) {
                                            CURRENT_TITLE = track.name;
                                            CURRENT_ARTIST = track.artist.name;
                                            CURRENT_ALBUM = track.album.name;
                                            Log.d("MainActivity", track.name + " by " + track.artist.name);
                                            ((TextView) findViewById(R.id.title)).setText(CURRENT_TITLE);
                                            ((TextView) findViewById(R.id.artist)).setText(CURRENT_ARTIST);
                                            ((TextView) findViewById(R.id.album)).setText(CURRENT_ALBUM);
                                        }
                                    }
                                });

                        //Build Initial Queue
                        new PerformSearchTask().execute("");
                        new PerformSearchTask().execute("");
                        new PerformSearchTask().execute("");
                        new PerformSearchTask().execute("");
                        new PerformSearchTask().execute("");

                        for (String uri : spotifyURIs) {
                            spotifyURIs.remove(uri);
                            mSpotifyAppRemote.getPlayerApi().queue(uri);
                        }

                        new PerformSearchTask().execute("");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    protected static String getAuthToken() { return AUTH_TOKEN; }

    protected static void addSpotifyURIToQueue(String uri) {spotifyURIs.add(uri);}

    public void playNextRandomSong(View v) {

        if (spotifyURIs.size() > 0) {
            String uri = spotifyURIs.remove(0);
            mSpotifyAppRemote.getPlayerApi().queue(uri);
        }

        mSpotifyAppRemote.getPlayerApi().skipNext();

        new PerformSearchTask().execute("");

        //TODO: Below is for testing to get AlarmManager to work
        /*Context context = this.getApplicationContext();

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, SpotifyAlarmHelper.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pi);*/
    }

    public void playButtonClicked(View v) {
        mSpotifyAppRemote.getPlayerApi().resume();

        findViewById(R.id.playButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
    }

    public void pauseButtonClicked(View v) {
        mSpotifyAppRemote.getPlayerApi().pause();

        findViewById(R.id.pauseButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.playButton).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    AUTH_TOKEN = response.getAccessToken();
                    Log.d("MainActivity","Successfully received Auth Token");
                    initializePlayer();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    //TODO: Figure out how to get AlarmManager working
    /*public class SpotifyAlarm extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }*/
}
