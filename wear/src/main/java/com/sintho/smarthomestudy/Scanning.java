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

public class Scanning extends Activity {
    private static final String LOGTAG = Scanning.class.getName();
    public static boolean scanning = false;
    public static final String ONFINISH = "onfinish";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView textView = (TextView) findViewById(R.id.scanTextView);
                        textView.setText(intent.getStringExtra(ONFINISH));
                    }
                }, new IntentFilter(ONFINISH)
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        scanning = true;
        Log.d(LOGTAG, "Setting registering to true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanning = false;
        Log.d(LOGTAG, "Setting registering to false, onStop");
    }
}
