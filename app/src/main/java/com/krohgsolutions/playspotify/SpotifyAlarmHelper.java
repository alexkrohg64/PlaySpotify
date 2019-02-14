package com.krohgsolutions.playspotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//TODO: Currently Alarm functionality doesn't work
public class SpotifyAlarmHelper extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.setAction("com.foo.ACTION");

        // Rebroadcasts to your own receiver.
        // This receiver is not exported; it'll only be received if the receiver is currently registered.
        context.sendBroadcast(i);
    }
}
