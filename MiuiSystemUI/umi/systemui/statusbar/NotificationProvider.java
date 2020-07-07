package com.android.systemui.statusbar;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.DatabaseHelper;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsManager;
import java.util.ArrayList;
import miui.util.Log;

public class NotificationProvider extends ContentProvider {
    public static final Uri URI_FOLD_IMPORTANCE = Uri.parse("content://statusbar.notification/foldImportance");
    private static final UriMatcher sMatcher;

    public String getType(Uri uri) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sMatcher = uriMatcher;
        uriMatcher.addURI("keyguard.notification", "notifications", 1);
        sMatcher.addURI("keyguard.notification", "notifications/#", 2);
        sMatcher.addURI("keyguard.notification", "app_corner", 3);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteDatabase openDB = openDB();
        MatrixCursor matrixCursor = null;
        if (openDB == null) {
            return null;
        }
        int match = sMatcher.match(uri);
        if (match == 1) {
            matrixCursor = openDB.query("notifications", strArr, str, strArr2, (String) null, (String) null, str2);
        } else if (match == 2) {
            matrixCursor = openDB.query("notifications", strArr, "_id=" + uri.getPathSegments().get(1) + parseSelection(str), strArr2, (String) null, (String) null, str2);
        } else if (match == 3) {
            if (strArr == null || strArr.length < 1) {
                return null;
            }
            MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"show_corner"});
            for (String canShowBadge : strArr) {
                matrixCursor2.addRow(new String[]{String.valueOf(NotificationSettingsHelper.canShowBadge(getContext(), canShowBadge) ? 1 : 0)});
            }
            matrixCursor = matrixCursor2;
        }
        if (matrixCursor != null) {
            matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return matrixCursor;
    }

    private String parseSelection(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return " AND (" + str + ')';
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase openDB = openDB();
        if (openDB == null) {
            return null;
        }
        long j = -1;
        if (sMatcher.match(uri) == 1) {
            j = openDB.insert("notifications", (String) null, contentValues);
        }
        return ContentUris.withAppendedId(uri, j);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        SQLiteDatabase openDB = openDB();
        if (openDB != null && sMatcher.match(uri) == 1) {
            return openDB.delete("notifications", str, strArr);
        }
        return 0;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        SQLiteDatabase openDB = openDB();
        if (openDB != null && sMatcher.match(uri) == 1) {
            return openDB.update("notifications", contentValues, str, strArr);
        }
        return 0;
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        Class cls = NotificationSettingsManager.class;
        if ("getKeyguardNotificationSortedKeys".equals(str)) {
            Bundle bundle2 = new Bundle();
            bundle2.putStringArrayList("sortedKeys", (ArrayList) ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).getSortedKeys());
            return bundle2;
        }
        String string = bundle.getString("package");
        String string2 = bundle.getString("channel_id");
        if (Constants.DEBUG) {
            Log.d("NotificationProvider", String.format("call method=%s extras=%s", new Object[]{str, bundle.toString()}));
        }
        Bundle bundle3 = new Bundle();
        if ("getFoldImportance".equals(str)) {
            bundle3.putInt("foldImportance", ((NotificationSettingsManager) Dependency.get(cls)).getFoldImportance(getContext(), string));
        } else if ("canShowBadge".equals(str)) {
            bundle3.putBoolean("canShowBadge", ((NotificationSettingsManager) Dependency.get(cls)).canShowBadge(getContext(), string));
        } else if ("canFloat".equals(str)) {
            bundle3.putBoolean("canShowFloat", ((NotificationSettingsManager) Dependency.get(cls)).canFloat(getContext(), string, string2));
        } else if ("canShowOnKeyguard".equals(str)) {
            bundle3.putBoolean("canShowOnKeyguard", ((NotificationSettingsManager) Dependency.get(cls)).canShowOnKeyguard(getContext(), string, string2));
        } else if ("canSound".equals(str)) {
            bundle3.putBoolean("canSound", ((NotificationSettingsManager) Dependency.get(cls)).canSound(getContext(), string));
        } else if ("canVibrate".equals(str)) {
            bundle3.putBoolean("canVibrate", ((NotificationSettingsManager) Dependency.get(cls)).canVibrate(getContext(), string));
        } else if ("canLights".equals(str)) {
            bundle3.putBoolean("canLights", ((NotificationSettingsManager) Dependency.get(cls)).canLights(getContext(), string));
        } else if (NotificationSettingsHelper.isUidSystem(Binder.getCallingUid())) {
            if ("setFoldImportance".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setFoldImportance(getContext(), string, bundle.getInt("foldImportance", 0));
            } else if ("setShowBadge".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setShowBadge(getContext(), string, bundle.getBoolean("canShowBadge", false));
            } else if ("setFloat".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setFloat(getContext(), string, string2, bundle.getBoolean("canShowFloat", false));
            } else if ("setShowOnKeyguard".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setShowOnKeyguard(getContext(), string, string2, bundle.getBoolean("canShowOnKeyguard", false));
            } else if ("setSound".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setSound(getContext(), string, bundle.getBoolean("canSound", false));
            } else if ("setVibrate".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setVibrate(getContext(), string, bundle.getBoolean("canVibrate", false));
            } else if ("setLights".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(cls)).setLights(getContext(), string, bundle.getBoolean("canLights", false));
            }
        }
        return bundle3;
    }

    private SQLiteDatabase openDB() {
        try {
            return DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
