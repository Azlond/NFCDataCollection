package com.sintho.smarthomestudy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

public class ScanningActivity extends Activity {
    private static final String LOGTAG = ScanningActivity.class.getName();
    public static boolean SCANNINGACTIVITY = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        //Listen to events sent by the NFCScanReceiverActivity, display NFC tag ID in UI
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView textView = (TextView) findViewById(R.id.scanTextView);
                        textView.setText(intent.getStringExtra(KEYS.ONFINISHINTENTFILTER));
                    }
                }, new IntentFilter(KEYS.ONFINISHINTENTFILTER)
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        //always reset the boolean to true when this view gets opened
        SCANNINGACTIVITY = true;
        Log.d(LOGTAG, "Setting registering to true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //always reset the boolean to false when this view gets closed
        SCANNINGACTIVITY = false;
        Log.d(LOGTAG, "Setting registering to false, onStop");
    }
}
