package com.sintho.nfcdatacollection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sintho.nfcdatacollection.communication.ReceiverService;

public class Registering extends Activity {
    private static final String LOGTAG = Registering.class.getName();
    public static boolean registering = false;
    public static final String ONFINISH = "onfinish";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registering);

        final Activity reg = this;
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        reg.finish();
                    }
                }, new IntentFilter(ONFINISH)
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        registering = true;
        Log.d(LOGTAG, "Setting registering to true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        registering = false;
        Log.d(LOGTAG, "Setting registering to false, onStop");
    }
}
