package com.sintho.smarthomestudy.db;

import android.provider.BaseColumns;

public final class DBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBContract() {
    }

    /* Inner class that defines the table contents */
    public static class DBEntry implements BaseColumns {
        public static final String TABLE_NAMENFCLOG = "nfclog";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NFCID = "nfcid";

        public static final String TABLE_NAMEREGISTER = "registerednfcnames";

        public static final String TABLE_UXSAMPLING = "uxsampling";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_DATA = "data";

    }
}
