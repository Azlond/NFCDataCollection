package com.sintho.nfcdatacollection.db;

import android.provider.BaseColumns;

public final class DBRegisterContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBRegisterContract() {
    }

    /* Inner class that defines the table contents */
    public static class DBRegisterEntry implements BaseColumns {
        public static final String TABLE_NAME = "registerednfcnames";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NFCID = "nfcid";
    }
}
