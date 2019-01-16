package com.sintho.nfcdatacollection.fragments;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.sintho.nfcdatacollection.Notifications;
import com.sintho.nfcdatacollection.R;
import com.sintho.nfcdatacollection.geo.GeoReceiver;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Frag_Settings extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final String LOGTAG = Frag_Settings.class.getName();
    private static final String LOCATIONCHECKBOX = "LOCATIONCHECKBOX";
    private static final String LOCATIONCHECKBOXTICKED = "LOCATIONCHECKBOXTICKED";
    public static final String PARTICIPANTIDSHARED = "PARTICIPANTIDSHARED";
    public static final String PARTICIPANTIDKEY = "PARTICIPANTIDKEY";
    private static final String REQUESTID = "1337";
    private static final int RADIUS = 100;
    private static final String LATITUDE = "LATITUDE";
    private static final String LONGITUDE = "LONGITUDE";

    private LocationManager lm;
    private GoogleApiClient mGoogleApiClient;

    private Button locationButton;
    private CheckBox locationCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frag__settings, container, false);
        locationButton = (Button) v.findViewById(R.id.locationButton);

        locationCheckBox = (CheckBox) v.findViewById(R.id.locationCheckbox);

        SharedPreferences prefs = getActivity().getSharedPreferences(LOCATIONCHECKBOX, MODE_PRIVATE);
        boolean ticked = prefs.getBoolean(LOCATIONCHECKBOXTICKED, false);
        locationCheckBox.setChecked(ticked);
        locationButton.setEnabled(ticked);

        locationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(LOCATIONCHECKBOX, MODE_PRIVATE).edit();
                editor.putBoolean(LOCATIONCHECKBOXTICKED, locationCheckBox.isChecked());
                editor.apply();
                locationButton.setEnabled(locationCheckBox.isChecked());

                if (locationCheckBox.isChecked()) {
                    SharedPreferences locPrefs = getActivity().getSharedPreferences(LOCATIONCHECKBOX, MODE_PRIVATE);
                    Double longitude = Double.parseDouble(locPrefs.getString(LONGITUDE, "200.0"));
                    Double latitude = Double.parseDouble(locPrefs.getString(LATITUDE, "200.0"));
                    if (longitude.equals(200.0) || latitude.equals(200.0)) {
                        Notifications.sendNotification(getContext(), getString(R.string.app_name), getString(R.string.notification_missingLocation), null, Notification.PRIORITY_MAX);
                    } else {
                        removeGeofence();
                        Location location = new Location("");
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        addNewGeofence(location);
                    }
                } else {
                    removeGeofence();
                }
            }
        });


        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, "New location for geofence set");
                if (mGoogleApiClient.isConnected()) {
                    updateLocation();
                }
            }
        });

        if (!mGoogleApiClient.isConnected()) {
            locationButton.setEnabled(false);
            locationCheckBox.setEnabled(false);
        }

        final EditText participantIDEditText = (EditText) v.findViewById(R.id.participantIDEditText);
        SharedPreferences prefs2 = getContext().getSharedPreferences(Frag_Settings.PARTICIPANTIDSHARED, MODE_PRIVATE);
        String token = prefs2.getString(Frag_Settings.PARTICIPANTIDKEY, "-1");
        if (!token.equals(("-1"))) {
            participantIDEditText.setText(token);
        }
        participantIDEditText.setCursorVisible(false);
        participantIDEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        participantIDEditText.setSingleLine(true);
        participantIDEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == participantIDEditText.getId()) {
                    participantIDEditText.setCursorVisible(true);
                }
            }
        });
        participantIDEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                participantIDEditText.setCursorVisible(false);
                return false;
            }
        });

        participantIDEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(participantIDEditText.getWindowToken(), 0);

                }
            }
        });
        participantIDEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(PARTICIPANTIDSHARED, MODE_PRIVATE).edit();
                editor.putString(PARTICIPANTIDKEY, String.valueOf(participantIDEditText.getText()));
                editor.apply();
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationHelpers();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient = null;
    }

    private void createLocationHelpers() {
        if (lm == null) {
            lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOGTAG, "Missing permission for location");
            Notifications.sendNotification(getContext(), getContext().getString(R.string.app_name), getString(R.string.notification_missingPermission), null, Notification.PRIORITY_MAX);
            return;
        }
        Location location = null;
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.i(LOGTAG, "GPS Provider");
        }
        if (location == null) {
            Log.w(LOGTAG, "No location available, requesting new");

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        } else {
            Log.i(LOGTAG, "normal location available");
            saveLocation(location);
            addNewGeofence(location);
        }
    }

    private void saveLocation(Location location) {
        Log.d(LOGTAG, "Saving location");
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(LOCATIONCHECKBOX, MODE_PRIVATE).edit();
        editor.putString(LATITUDE, Double.toString(location.getLatitude()));
        editor.putString(LONGITUDE, Double.toString(location.getLongitude()));
        editor.apply();
    }

    private void addNewGeofence(Location location) {
        Log.d(LOGTAG, "Adding Geofence");
        Log.d(LOGTAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        Geofence geofence = new Geofence.Builder()
                .setRequestId(REQUESTID)
                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        /* The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
         * GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
         * is already inside that geofence.
         */
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence);


        Intent intent = new Intent(getContext(), GeoReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(LOGTAG, "added pendingintent with broadcast");
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                builder.build(),
                pi
        ).setResultCallback(this);
    }

    /**
     * setResultCallback()-Method after adding/removing geofences
     */
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(LOGTAG, "Geofences have been added/removed");

        } else {
            Log.w(LOGTAG, Integer.toString(status.getStatusCode()));
            if (status.getStatusCode() == 1000) {
                Log.w(LOGTAG, "A higher location mode (Settings-Location-Mode) needs to be enabled");
                Log.w(LOGTAG, "This might also have a different cause");
                Notifications.sendNotification(getContext(), getString(R.string.app_name), getString(R.string.notification_highAccuracy), null, Notification.PRIORITY_MAX);
            }
        }
    }

    private void removeGeofence() {
        Log.d(LOGTAG, "Removing Geofence");
        List<String> list = new ArrayList<>();
        list.add(REQUESTID);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, list);
    }






    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOGTAG, "Location changed");
        lm.removeUpdates(this);
        saveLocation(location);
        addNewGeofence(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOGTAG, "GoogleAPIClient connected");
        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (locationCheckBox != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(LOCATIONCHECKBOX, MODE_PRIVATE);
            boolean ticked = prefs.getBoolean(LOCATIONCHECKBOXTICKED, false);
            locationCheckBox.setEnabled(true);
            locationButton.setEnabled(ticked);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
