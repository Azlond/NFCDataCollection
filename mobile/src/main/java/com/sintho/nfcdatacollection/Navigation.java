package com.sintho.nfcdatacollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sintho.nfcdatacollection.communication.ReceiverService;
import com.sintho.nfcdatacollection.communication.TransmitService;
import com.sintho.nfcdatacollection.fragments.Frag_Contact;
import com.sintho.nfcdatacollection.fragments.Frag_NFCLog;
import com.sintho.nfcdatacollection.fragments.Frag_NFCRegister;

import java.util.List;

import static com.google.android.gms.wearable.CapabilityApi.*;

public class Navigation extends AppCompatActivity implements NodeApi.NodeListener {
    //TODO: replace icons in drawer
    private static final String LOGTAG = Navigation.class.getName();
    /**
     * Navigation tab names
     */
    private static final String TAG_LOG = "log";
    private static final String TAG_REGISTER = "register";
    private static final String TAG_CONTACT = "contact";

    // index to identify current nav menu item
    private static int navItemIndex = 0;
    private static String CURRENT_TAG = TAG_LOG;

    private Handler mHandler;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;
    private BroadcastReceiver receiver;

    private CheckBox wearableConnected;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activityTitles = getResources().getStringArray(R.array.nav_names);
        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_LOG;
            loadHomeFragment();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOGTAG, "received message to switch fragments");
                if (CURRENT_TAG.equals(TAG_REGISTER)) {
                    return;
                }
                CURRENT_TAG = TAG_REGISTER;
                navItemIndex = 1;
                loadHomeFragment();
            }
        };

        //register receiver
        Log.d(LOGTAG, "registering broadcast-receiver");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter(ReceiverService.FRAGREGISTER)
        );

        //add functionality for sync button
        Button syncButton = (Button) findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent syncIntent = new Intent(getApplicationContext(), TransmitService.class);
                syncIntent.putExtra(TransmitService.TASK, TransmitService.SYNC);
                startService(syncIntent);
            }
        });
        wearableConnected = (CheckBox) findViewById(R.id.connectIndicator);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect();
                final List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectedNodes == null || connectedNodes.isEmpty()) {
                            wearableConnected.setChecked(false);
                        } else {
                            wearableConnected.setChecked(true);
                        }}
                });

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //remove registered listener, we no longer need updates if the fragment is not visible
        if (receiver != null) {
            Log.d(LOGTAG, "unregistering broadcast-receiver");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        mHandler.post(mPendingRunnable);

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                return new Frag_NFCLog();
            case 1:
                return new Frag_NFCRegister();
            case 2:
                return new Frag_Contact();
            default:
                return new Frag_NFCLog();
        }
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_log:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_LOG;
                        break;
                    case R.id.nav_register:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_REGISTER;
                        break;
                    case R.id.nav_contact:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_CONTACT;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {};

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onPeerConnected(Node node) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOGTAG, "Device connected, updating UI");
                wearableConnected.setChecked(true);
            }
        });
    }

    @Override
    public void onPeerDisconnected(Node node) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOGTAG, "Device disconnected, updating UI");
                wearableConnected.setChecked(false);
            }
        });    }
}
