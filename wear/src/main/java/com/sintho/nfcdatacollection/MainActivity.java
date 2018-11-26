package com.sintho.nfcdatacollection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    public static final String SHAREDPREFERENCESKEY = "NFCSharedPrefereces";
    public static final String BATTERYNOTIFICATION = "BatteryNotification";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent("com.sintho.nfcdatacollection.SYNC_SERVICE");
        sendBroadcast(i);

        final Button registerButton = (Button) findViewById(R.id.dbButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Log.d("MAIN", "register clicked");
                    Intent i = new Intent(getApplicationContext(), Scanning.class);
                    startActivity(i);
            }
        });
    }
}
