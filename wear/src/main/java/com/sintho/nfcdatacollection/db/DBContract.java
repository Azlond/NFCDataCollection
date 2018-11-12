package com.sintho.nfcdatacollection.db;

import android.provider.BaseColumns;

public final class DBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBContract() {
    }

    /* Inner class that defines the table contents */
    public static class DBEntry implements BaseColumns {
        public static final String TABLE_NAME = "dataset";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NFCID = "nfcid";
        public static final String COLUMN_SYNCED = "synced";
    }
}
