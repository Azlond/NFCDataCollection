package com.sintho.smarthomestudy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(KEYS.SYNCSERVICEINTENTFILTER);
        sendBroadcast(i);

        final Button registerButton = (Button) findViewById(R.id.dbButton);
        //button to enter the scanning activity
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), ScanningActivity.class);
                    startActivity(i);
            }
        });
    }
}
