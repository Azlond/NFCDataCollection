package com.sintho.nfcdatacollection;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sintho.nfcdatacollection.db.DBContract;
import com.sintho.nfcdatacollection.db.DBHelper;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DBHelper mDbHelper = new DBHelper(getApplicationContext());
        Button button = (Button) findViewById(R.id.dbButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                // Define a projection that specifies which columns from the database
                // you will actually use after this query.

                String sortOrder =
                        DBContract.DBEntry.COLUMN_DATE+ " DESC";
                Cursor cursor = db.query(
                        DBContract.DBEntry.TABLE_NAME,   // The table to query
                        null,             // The array of columns to return (pass null to get all)
                        null,              // The columns for the WHERE clause
                        null,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );


                List itemIds = new ArrayList<>();
                List dates = new ArrayList<>();
                List nfcIds = new ArrayList<>();
                while(cursor.moveToNext()) {
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DBContract.DBEntry._ID));
                    itemIds.add(itemId);
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_DATE));
                    dates.add(date);
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NFCID));
                    nfcIds.add(id);
                }
                cursor.close();
                for (int i = 0; i < itemIds.size(); i++) {
                    Log.d(MainActivity.class.getName(), MessageFormat.format("DB-ID: {0}; Date: {1}; NFC-ID: {2}", itemIds.get(i), dates.get(i), nfcIds.get(i)));
                }

            }
        });
    }
}
