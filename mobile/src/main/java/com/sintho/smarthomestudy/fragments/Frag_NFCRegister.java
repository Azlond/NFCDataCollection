package com.sintho.smarthomestudy.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sintho.smarthomestudy.R;
import com.sintho.smarthomestudy.communication.ReceiverService;
import com.sintho.smarthomestudy.db.DBContract;
import com.sintho.smarthomestudy.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

/***
 * Fragment that displays the registered nfc-tags
 */
public class Frag_NFCRegister extends Fragment {
    private static final String LOGTAG = Frag_NFCRegister.class.getName();
    private BroadcastReceiver receiver;
    public Frag_NFCRegister() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.frameRegisterLayout);
                if (frameLayout == null) {
                    throw new NullPointerException("Layout does not exist");
                }
                TableLayout tableLayout = fillRegister();
                frameLayout.removeAllViewsInLayout();
                ScrollView scrollView = new ScrollView(getContext());
                scrollView.addView(tableLayout);
                frameLayout.addView(scrollView);
            }
        };

        //register receiver
        Log.d(LOGTAG, "registering broadcast-receiver");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                receiver, new IntentFilter(ReceiverService.FRAGREGISTER)
        );
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_frag__nfcregister, container, false);
        TableLayout tableLayout = fillRegister();
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
     * @return textview
     */
    private EditText createTableRowEditText(String text, int width) {
        final EditText editText= new EditText(getContext());
        editText.setText(text);
        editText.setWidth(width);
        editText.setMaxWidth(width);
        editText.setPadding(25,2,0,0);
        editText.setCursorVisible(false);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSingleLine(true);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == editText.getId()) {
                    editText.setCursorVisible(true);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                editText.setCursorVisible(false);
                return false;
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                }
            }
        });
        return  editText;
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
        textView.setWidth(width);
        textView.setMaxWidth(width);
        textView.setPadding(leftPadding,2,0,0);
        return  textView;
    }

    private Button createSaveButton(boolean enabled, int width, final TextView nfcID, final EditText name) {
        Button button = new Button(getContext());
        button.setText(R.string.save);
        button.setEnabled(enabled);
        button.setMaxWidth(width);
        button.setWidth(width);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, String.format("Clicked id %s with new name %s", nfcID.getText(), name.getText()));
                //update id->name database
                DBHelper mDbRegisterHelper = new DBHelper(getContext());
                SQLiteDatabase registerDB = mDbRegisterHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBContract.DBEntry.COLUMN_NAME, String.valueOf(name.getText()));
                registerDB.update(DBContract.DBEntry.TABLE_NAMEREGISTER, values, DBContract.DBEntry.COLUMN_NFCID + " = ?", new String[]{String.valueOf(nfcID.getText())});
                registerDB.close();
                DBHelper mdbHelper = new DBHelper(getContext());
                SQLiteDatabase logDB = mdbHelper.getWritableDatabase();
                ContentValues nameValue = new ContentValues();
                nameValue.put(DBContract.DBEntry.COLUMN_NAME, String.valueOf(name.getText()));
                logDB.update(DBContract.DBEntry.TABLE_NAMENFCLOG, nameValue, DBContract.DBEntry.COLUMN_NFCID + " = ?", new String[]{String.valueOf(nfcID.getText())});
                logDB.close();
                Toast.makeText(getActivity(), R.string.savedNewName, Toast.LENGTH_LONG).show();
            }
        });
        return button;
    }

    private TableLayout fillRegister() {
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

        headerRow.addView(createTableHeaderView(R.string.id, width * 2/5, 0));
        headerRow.addView(createTableHeaderView(R.string.name, width * 2/5, 25));
        headerRow.addView(createTableHeaderView(R.string.save, width / 5, 25));

        tl.addView(headerRow);

        DBHelper mDbRegisterHelper = new DBHelper(getContext());
        SQLiteDatabase db = mDbRegisterHelper.getReadableDatabase();
        //Sort chronologically by ID, newest first
        String sortOrder = DBContract.DBEntry.COLUMN_NFCID + " DESC";
        //get all entries
        Cursor cursor = db.query(
                DBContract.DBEntry.TABLE_NAMEREGISTER,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        //add all required elements to lists
        List<String> names = new ArrayList<>();
        List<String> nfcIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME));
            names.add(name);
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NFCID));
            nfcIds.add(id);
        }
        cursor.close();
        db.close();

        /*
         * if the query returns result, display them
         * otherwise, show a default note
         */
        Log.d(LOGTAG, "adding content to table layout");
        if (nfcIds.size() > 0) {
            for (int i = 0; i < nfcIds.size(); i++) {
                TableRow tr = new TableRow(getContext());
                TextView nfcID = createTableRowView(nfcIds.get(i), width * 2/5, 0);
                tr.addView(nfcID);

                EditText nfcName = createTableRowEditText(names.get(i), width * 2/5);
                tr.addView(nfcName);
                tr.addView(createSaveButton(true, width / 5, nfcID, nfcName));
                tl.addView(tr);
            }
        } else {
            //Display a default note that this is where the Tag info will be displayed
            TableRow tr = new TableRow(getContext());

            tr.addView(createTableRowView(getString(R.string.tableValueMissingID), width * 2/5, 0));
            tr.addView(createTableRowView(getString(R.string.tableValueMissingName), width * 2/5, 25));
            tr.addView(createSaveButton(false, width / 5, null, null));

            tl.addView(tr);
        }
        return tl;
    }
}
