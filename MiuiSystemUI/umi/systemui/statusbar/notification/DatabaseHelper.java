package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static volatile DatabaseHelper mDatabaseHelper;

    private DatabaseHelper(Context context) {
        super(context, (String) null, (SQLiteDatabase.CursorFactory) null, 15);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (mDatabaseHelper == null) {
            synchronized (DatabaseHelper.class) {
                if (mDatabaseHelper == null) {
                    mDatabaseHelper = new DatabaseHelper(context);
                }
            }
        }
        return mDatabaseHelper;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE  IF NOT EXISTS notifications (_id INTEGER PRIMARY KEY AUTOINCREMENT,icon BLOB,title TEXT,content TEXT,time TEXT,info TEXT,subtext TEXT,key INTEGER,pkg TEXT,user_id INTEGER);");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        Log.w("DatabaseHelper", "Upgrading settings database from version " + i + " to " + i2);
        if (i != i2) {
            Log.w("DatabaseHelper", "Got stuck trying to upgrade from version " + i + ", must wipe the settings provider");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS notifications");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS notification_sort");
            onCreate(sQLiteDatabase);
        }
    }

    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i != i2) {
            Log.w("DatabaseHelper", "Got stuck trying to upgrade from version " + i + ", must wipe the settings provider");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS notifications");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS notification_sort");
            onCreate(sQLiteDatabase);
        }
    }
}
