package com.sintho.smarthomestudy.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.sintho.smarthomestudy.R;

import java.io.IOException;

public class RegistrationService extends IntentService {
    private static final String LOGTAG = RegistrationService.class.getName();

    public RegistrationService() {
        super("RegistrationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = "";
        try {
            token = instanceID.getToken(this.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOGTAG, "Token is: " + token);
        GcmPubSub subscription = GcmPubSub.getInstance(this);
        try {
            subscription.subscribe(token, getString(R.string.subscriptionID), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
