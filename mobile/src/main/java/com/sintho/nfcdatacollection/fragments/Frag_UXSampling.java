package com.sintho.nfcdatacollection.fragments;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sintho.nfcdatacollection.MaxHeightScrollView;
import com.sintho.nfcdatacollection.R;
import com.sintho.nfcdatacollection.db.DBContract;
import com.sintho.nfcdatacollection.db.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Frag_UXSampling extends Fragment {
    private final String LOGTAG = Frag_UXSampling.class.getName();
    private TextView dateView;
    private EditText UXNotes;
    private ArrayList<CheckBox> checkBoxes;
    private final String NAMES = "names";
    private final String IDS = "ids";
    private final String CURSOR = "cursor";
    private final String DB = "db";
    public Frag_UXSampling() {
        // Required empty public constructor
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_frag__uxsampling, container, false);
        Calendar mcurrentDate=Calendar.getInstance();
        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH) + 1;
        final int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        dateView = (TextView) v.findViewById(R.id.dateTextView);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To show current date in the datepicker
                DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("DefaultLocale")
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        dateView.setText(String.format("%d-%d-%d", selectedyear, selectedmonth + 1, selectedday));
                        Log.d(LOGTAG, "Setting new Date");
                        HashMap<String, Object> map = readPreviousEntries();
                        Cursor cursor = (Cursor) map.get(CURSOR);
                        try {
                            createLayout(v);
                        } finally {
                            cursor.close();
                            SQLiteDatabase db = (SQLiteDatabase) map.get(DB);
                            db.close();

                        }
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        };

        dateView.setText(String.format("%d-%d-%d", mYear, mMonth, mDay));
        dateView.setOnClickListener(onClickListener);
        v.findViewById(R.id.editTextView).setOnClickListener(onClickListener);
        createLayout(v);
        return v;
    }

    private void createLayout(View v) {
        FrameLayout frameLayout = (FrameLayout) v.findViewById(R.id.UXSamplingFrameLayout);
        TableLayout tableLayout = fillUXSampling();
        frameLayout.removeAllViews();
//        ScrollView scrollView = new ScrollView(getContext());
        MaxHeightScrollView scrollView = new MaxHeightScrollView(getContext());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;
        scrollView.setMaxHeight((height - 50) * 3/5);
        scrollView.addView(tableLayout);
        frameLayout.addView(scrollView);

        UXNotes = (EditText) v.findViewById(R.id.UXNotesEditText);
        UXNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(UXNotes.getWindowToken(), 0);
                    }

                }
            }
        });
        HashMap<String, Object> map = readPreviousEntries();
        Cursor cursor = (Cursor) map.get(CURSOR);
        try {

            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                UXNotes.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_TEXT)));
            } else {
                UXNotes.setText("");
            }
        } finally {
            cursor.close();
            SQLiteDatabase db = (SQLiteDatabase) map.get(DB);
            db.close();
        }

        v.findViewById(R.id.UXSamplingSaveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, List<String>> map = getIDsAndNames();
                List<String> nfcIds = map.get(IDS);
                List<String> names = map.get(NAMES);

                StringBuilder data = new StringBuilder();

                for (int i = 0; i < nfcIds.size(); i++) {
                    String nfcID = nfcIds.get(i);
                    String name = names.get(i);
                    boolean checked = checkBoxes.get(i).isChecked();
                    data.append(String.format("%s/%s: %b\n", nfcID, name, checked));
                }

                String notes = String.valueOf(UXNotes.getText());
                DBHelper mDbHelper = new DBHelper(getContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DBContract.DBEntry.COLUMN_DATE, String.valueOf(dateView.getText()));
                values.put(DBContract.DBEntry.COLUMN_TEXT, notes);
                values.put(DBContract.DBEntry.COLUMN_DATA, data.toString().toString());

                long newRowId = db.replace(DBContract.DBEntry.TABLE_UXSAMPLING, null, values);
                if (newRowId == -1) {
                    try {
                        throw new Exception(String.format("row not inserted: %d", newRowId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Toast.makeText(getActivity(), R.string.toast_successSave, Toast.LENGTH_LONG).show();
            }
        });
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
    private TextView createTableRowView(String text, int width) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setWidth(width);
        textView.setMaxWidth(width);
        textView.setPadding(0,2,0,0);
        return  textView;
    }

    private CheckBox createCheckBox(int width, boolean checked) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setEnabled(true);
        checkBox.setMaxWidth(width);
        checkBox.setWidth(width);
        checkBox.setChecked(checked);
        return checkBox;
    }

    private TableLayout fillUXSampling() {
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

        headerRow.addView(createTableHeaderView(R.string.id, width * 4/5, 0));
        headerRow.addView(createTableHeaderView(R.string.name, width * 2/5, 25));
        headerRow.addView(createTableHeaderView(R.string.forgotten, width / 5, 25));

        tl.addView(headerRow);

        HashMap<String, List<String>> map = getIDsAndNames();
        List<String> nfcIds = map.get(IDS);
        List<String> names = map.get(NAMES);
        /*
         * if the query returns result, display them
         * otherwise, show a default note
         */
        Log.d(LOGTAG, "adding content to table layout");

        HashMap<String, Object> hashMap = new HashMap<>();

        HashMap<String, Object> dbMap = readPreviousEntries();
        Cursor cursor = (Cursor) dbMap.get(CURSOR);
        try {
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_DATA));
                String[] elements = data.split("\n");
                for (String part: elements) {
                    String[] parts = part.split("/");
                    String id = parts[0];
                    Boolean checked = parts[1].substring(parts[1].length() - 4).equals("true");
                    hashMap.put(id, checked);
                }
            }
        } finally {
            cursor.close();
            SQLiteDatabase db = (SQLiteDatabase) dbMap.get(DB);
            db.close();
        }

        checkBoxes = new ArrayList<>();
        if (nfcIds.size() > 0) {
            for (int i = 0; i < nfcIds.size(); i++) {
                TableRow tr = new TableRow(getContext());
                TextView nfcID = createTableRowView(nfcIds.get(i), width * 2/5);
                tr.addView(nfcID);

                TextView nfcName = createTableRowView(names.get(i), width * 2/5);
                tr.addView(nfcName);
                boolean checked = hashMap.containsKey(String.valueOf(nfcID.getText())) && (boolean) hashMap.get(String.valueOf(nfcID.getText()));
                CheckBox checkBox = createCheckBox(width / 5, checked);
                tr.addView(checkBox);
                checkBoxes.add(i, checkBox);
                tl.addView(tr);
            }
        }
        return tl;
    }

    private HashMap<String, Object> readPreviousEntries() {
        DBHelper mDbHelper = new DBHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sortOrder = DBContract.DBEntry.COLUMN_DATE + " DESC";
        Log.d(LOGTAG, "Retrieving values from" + String.valueOf(dateView.getText()));
        Cursor cursor = db.query(
                DBContract.DBEntry.TABLE_UXSAMPLING,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                DBContract.DBEntry.COLUMN_DATE + " = ?",              // The columns for the WHERE clause
                new String[]{String.valueOf(dateView.getText())},          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        HashMap<String, Object> map = new HashMap<>();
        map.put(CURSOR, cursor);
        map.put(DB, db);
        return map;
    }

    private HashMap<String, List<String>> getIDsAndNames() {
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

        HashMap<String, List<String>> map = new HashMap<>();
        map.put(NAMES, names);
        map.put(IDS, nfcIds);
        return map;
    }


}
