package com.sintho.nfcdatacollection.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sintho.nfcdatacollection.R;
import com.sintho.nfcdatacollection.db.DBLogContract;
import com.sintho.nfcdatacollection.db.DBLogHelper;
import com.sintho.nfcdatacollection.db.DBRegisterContract;
import com.sintho.nfcdatacollection.db.DBRegisterHelper;

import java.util.ArrayList;
import java.util.List;

/***
 * Fragment that displays the registered nfc-tags
 */
public class Frag_NFCRegister extends Fragment {
    private static final String LOGTAG = Frag_NFCRegister.class.getName();
    public Frag_NFCRegister() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_frag__nfcregister, container, false);
        TableLayout tableLayout = fillRegister();
        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(tableLayout);
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
                DBRegisterHelper mDbRegisterHelper = new DBRegisterHelper(getContext());
                SQLiteDatabase registerDB = mDbRegisterHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBRegisterContract.DBRegisterEntry.COLUMN_NAME, String.valueOf(name.getText()));
                registerDB.update(DBRegisterContract.DBRegisterEntry.TABLE_NAME, values, DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " = ?", new String[]{String.valueOf(nfcID.getText())});

                DBLogHelper mdbLogHelper = new DBLogHelper(getContext());
                SQLiteDatabase logDB = mdbLogHelper.getWritableDatabase();
                ContentValues nameValue = new ContentValues();
                nameValue.put(DBLogContract.DBLogEntry.COLUMN_NAME, String.valueOf(name.getText()));
                logDB.update(DBLogContract.DBLogEntry.TABLE_NAME, nameValue, DBLogContract.DBLogEntry.COLUMN_NFCID + " = ?", new String[]{String.valueOf(nfcID.getText())});
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

        DBRegisterHelper mDbRegisterHelper = new DBRegisterHelper(getContext());
        SQLiteDatabase db = mDbRegisterHelper.getReadableDatabase();
        //Sort chronologically by ID, newest first
        String sortOrder = DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " DESC";
        //get all entries
        Cursor cursor = db.query(
                DBRegisterContract.DBRegisterEntry.TABLE_NAME,   // The table to query
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
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBRegisterContract.DBRegisterEntry.COLUMN_NAME));
            names.add(name);
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBRegisterContract.DBRegisterEntry.COLUMN_NFCID));
            nfcIds.add(id);
        }
        cursor.close();

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
