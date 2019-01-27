package com.example.klaudiachylarecka.silencer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Date;

public final class HoursContract {

    private  HoursContract() {

    }

    public static class Hour implements BaseColumns {
        public static final String TABLE_NAME = "hours";
        public static final String COLUMN_NAME_START_TIME = "stime";
        public static final String COLUMN_NAME_END_TIME = "etime";
        public static final String COLUMN_NAME_DAY = "day";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Hour.TABLE_NAME + " (" +
                        Hour._ID + " INTEGER PRIMARY KEY," +
                        Hour.COLUMN_NAME_START_TIME + " TEXT," +
                        Hour.COLUMN_NAME_END_TIME + " TEXT," +
                        Hour.COLUMN_NAME_DAY + " TEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Hour.TABLE_NAME;

    }
}


