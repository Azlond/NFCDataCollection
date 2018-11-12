package com.sintho.nfcdatacollection.db;

import android.provider.BaseColumns;

public final class DBLogContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBLogContract() {
    }

    /* Inner class that defines the table contents */
    public static class DBLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "nfclog";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NFCID = "nfcid";
    }
}
