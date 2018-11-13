package com.sintho.nfcdatacollection.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sintho.nfcdatacollection.R;
import com.sintho.nfcdatacollection.communication.ReceiverService;
import com.sintho.nfcdatacollection.db.DBLogContract;
import com.sintho.nfcdatacollection.db.DBLogHelper;

import java.util.ArrayList;
import java.util.List;

/***
 * Fragment that displays the logged nfc-events
 */
public class Frag_NFCLog extends Fragment {
    private BroadcastReceiver receiver;
    private static final String LOGTAG = Frag_NFCLog.class.getName();


    public Frag_NFCLog() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //remove registered listener, we no longer need updates if the fragment is not visible
        if (receiver != null) {
            Log.d(LOGTAG, "unregistering broadcast-receiver");
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create broadcastreceiver that updates the fragment when new nfc events are sent
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //noinspection ConstantConditions
                FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.frameLogLayout);
                if (frameLayout == null) {
                    throw new NullPointerException("Layout does not exist");
                }
                TableLayout tableLayout = fillLog();
                frameLayout.removeAllViewsInLayout();
                ScrollView scrollView = new ScrollView(getContext());
                scrollView.addView(tableLayout);
                frameLayout.addView(scrollView);
            }
        };

        //register receiver
        Log.d(LOGTAG, "registering broadcast-receiver");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                receiver, new IntentFilter(ReceiverService.NFCTAGCAST)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_frag__nfclog, container, false);

        // Add the database logs to tableLayout, add tableLayout to fragment
        TableLayout tableLayout = fillLog();
        frameLayout.removeAllViewsInLayout();
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(tableLayout);
        frameLayout.addView(scrollView);
        return frameLayout;
    }

    /**
     * creates and styles the textviews for the column titles
     * @param RID R.string.* id
     * @param leftPadding padding to the left
     * @return styled textView
     */
    private TextView createTableHeaderView(int RID, int width, int leftPadding) {
        TextView textView = new TextView(getContext());
        SpannableString text = new SpannableString(getString(RID));
        text.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
        text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), 0);
        textView.setText(text);
        textView.setMaxWidth(width);
        textView.setPadding(leftPadding, 0, 0, 0);
        return textView;
    }

    /**
     * creates the textviews for the rows
     * @param text text to display
     * @param width max width of the column
     * @param leftPadding padding to the left
     * @return textview
     */
    private TextView createTableRowView(String text, int width, int leftPadding) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setMaxWidth(width);
        textView.setPadding(leftPadding,2,0,0);
        return  textView;
    }

    /**
     * creates a table layout, queries the database and adds the query-result to the layout
     * @return tablelayout
     */
    private TableLayout fillLog() {
        //calculate display width
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        TableLayout tl = new TableLayout(getContext());

        /*
         * Create header row, where column titles are displayed
         */
        TableRow headerRow = new TableRow(getContext());

        headerRow.addView(createTableHeaderView(R.string.id, width / 5, 0));
        headerRow.addView(createTableHeaderView(R.string.date, width * 2/5, 25));
        headerRow.addView(createTableHeaderView(R.string.name, width * 2/5, 25));

        tl.addView(headerRow);

        DBLogHelper mDbLogHelper = new DBLogHelper(getContext());
        SQLiteDatabase db = mDbLogHelper.getReadableDatabase();
        //Sort chronologically by ID, newest first
        String sortOrder = DBLogContract.DBLogEntry.COLUMN_ID+ " DESC";
        //get all entries
        Cursor cursor = db.query(
                DBLogContract.DBLogEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        //add all required elements to lists
        List<Long> itemIds = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> nfcIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBLogContract.DBLogEntry.COLUMN_ID));
            itemIds.add(itemId);
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBLogContract.DBLogEntry.COLUMN_NAME));
            names.add(name);
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBLogContract.DBLogEntry.COLUMN_DATE));
            dates.add(date);
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBLogContract.DBLogEntry.COLUMN_NFCID));
            nfcIds.add(id);
        }
        cursor.close();

        /*
         * if the query returns result, display them
         * otherwise, show a default note
         */
        Log.d(LOGTAG, "adding content to table layout");
        if (itemIds.size() > 0) {
            for (int i = 0; i < itemIds.size(); i++) {
                TableRow tr = new TableRow(getContext());
                String name;
                //if the tag has a name, use it. Otherwise use the NFC-ID
                if (names.get(i).length() == 0) {
                    name = nfcIds.get(i);
                } else {
                    name = names.get(i);
                }
                tr.addView(createTableRowView(String.valueOf(itemIds.get(i)), width / 5, 0));
                tr.addView(createTableRowView(dates.get(i), width * 2/5, 25));
                tr.addView(createTableRowView(name, width * 2/5, 25));

                tl.addView(tr);
            }
        } else {
            //Display a default note that this is where the Tag info will be displayed
            TableRow tr = new TableRow(getContext());

            tr.addView(createTableRowView(getString(R.string.tableValueMissingID), width / 5, 0));
            tr.addView(createTableRowView(getString(R.string.tableValueMissingDate), width * 2/5, 25));
            tr.addView(createTableRowView(getString(R.string.tableValueMissingName), width * 2/5, 25));

            tl.addView(tr);
        }
        return tl;
    }
}
