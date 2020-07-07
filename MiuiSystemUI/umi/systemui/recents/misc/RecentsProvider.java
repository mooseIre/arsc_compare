package com.android.systemui.recents.misc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import java.util.List;

public class RecentsProvider extends ContentProvider {
    private static final UriMatcher sURIMatcher;
    private MatrixCursor mForceMultiWindowPkgCursor;
    private MatrixCursor mForceNotMultiWindowPkgCursor;

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sURIMatcher = uriMatcher;
        uriMatcher.addURI("com.miui.systemui.recents", "MULTI_WINDOW_FORCE_RESIZE_PKGS", 1);
        sURIMatcher.addURI("com.miui.systemui.recents", "MULTI_WINDOW_FORCE_NOT_RESIZE_PKGS", 2);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        Log.d("RecentsProvider", "query uri=" + uri);
        int match = sURIMatcher.match(uri);
        if (match == 1) {
            if (this.mForceMultiWindowPkgCursor == null) {
                List<String> multiWindowForceResizeList = SystemServicesProxy.getMultiWindowForceResizeList(getContext());
                this.mForceMultiWindowPkgCursor = new MatrixCursor(new String[]{"pkgs"});
                for (int i = 0; i < multiWindowForceResizeList.size(); i++) {
                    this.mForceMultiWindowPkgCursor.addRow(new String[]{multiWindowForceResizeList.get(i)});
                }
            }
            return this.mForceMultiWindowPkgCursor;
        } else if (match == 2) {
            if (this.mForceNotMultiWindowPkgCursor == null) {
                List<String> multiWindowForceNotResizeList = SystemServicesProxy.getMultiWindowForceNotResizeList(getContext());
                this.mForceNotMultiWindowPkgCursor = new MatrixCursor(new String[]{"pkgs"});
                for (int i2 = 0; i2 < multiWindowForceNotResizeList.size(); i2++) {
                    this.mForceNotMultiWindowPkgCursor.addRow(new String[]{multiWindowForceNotResizeList.get(i2)});
                }
            }
            return this.mForceNotMultiWindowPkgCursor;
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
