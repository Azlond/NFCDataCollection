package com.sintho.nfcdatacollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.nfcTagName);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String id = intent.getStringExtra(ReceiverService.NFCTAGID);
                        Log.d(MainActivity.class.getName(), id);
                        textView.setText("NFC ID: " + id);
                        textView.setHint("NFC ID: " + id);
                    }
                }, new IntentFilter(ReceiverService.NFCTAGCAST)
        );
    }
}
