package com.sintho.smarthomestudy;

public class KEYS {
    public static final String SHAREDPREFERENCESKEY = "NFCSharedPrefereces";

    public static final String SYNCSERVICEINTENTFILTER = "com.sintho.smarthomestudy.SYNC_SERVICE"; //same as in the manifest receiver declaration
    public static final String ONFINISHINTENTFILTER = "onfinish"; //intent filter for the SCANNINGACTIVITY activity

    public static final String SCANTAGNOLOG = "SCANTAGNOLOG";
    public static final String JSONBYTEARRAY = "JSONBYTEARRAY";

    //JSON Keys:
    public static final String IDSTRING = "id";
    public static final String DATESTRING = "date";
    public static final String NFCIDSTRING = "nfcid";



    //for communication with phone
    public static final String BATTERYNOTIFICATION = "BatteryNotification";
    public static final String RECEIVED = "RECEIVED";
    public static final String SYNC = "SYNC";


    //phone communication keys
    public static final String TAGFOUND = "FOUND_TAG";
}
