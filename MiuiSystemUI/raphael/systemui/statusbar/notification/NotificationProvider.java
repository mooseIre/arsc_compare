package com.android.systemui.statusbar.notification;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.policy.KeyguardNotificationController;
import java.util.ArrayList;
import miui.util.Log;

public class NotificationProvider extends ContentProvider {
    public static final Uri URI_FOLD_IMPORTANCE = Uri.parse("content://statusbar.notification/foldImportance");
    private static final UriMatcher sMatcher;
    private final ContentObserver mFoldImportanceObserver = new ContentObserver(this, (Handler) Dependency.get(Dependency.MAIN_HANDLER)) {
        /* class com.android.systemui.statusbar.notification.NotificationProvider.AnonymousClass1 */

        public void onChange(boolean z, Uri uri) {
            try {
                NotificationSettingsHelper.setFoldImportance(uri.getQueryParameter("package"), Integer.parseInt(uri.getQueryParameter("foldImportance")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sMatcher = uriMatcher;
        uriMatcher.addURI("keyguard.notification", "notifications", 1);
        sMatcher.addURI("keyguard.notification", "notifications/#", 2);
        sMatcher.addURI("keyguard.notification", "app_corner", 3);
    }

    public boolean onCreate() {
        getContext().getContentResolver().registerContentObserver(URI_FOLD_IMPORTANCE, false, this.mFoldImportanceObserver, -1);
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteDatabase openDB = openDB();
        Cursor cursor = null;
        if (openDB == null) {
            return null;
        }
        int match = sMatcher.match(uri);
        if (match == 1) {
            cursor = openDB.query("notifications", strArr, str, strArr2, null, null, str2);
        } else if (match == 2) {
            cursor = openDB.query("notifications", strArr, "_id=" + uri.getPathSegments().get(1) + parseSelection(str), strArr2, null, null, str2);
        } else if (match == 3) {
            if (strArr == null || strArr.length < 1) {
                return null;
            }
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"show_corner"});
            for (String str3 : strArr) {
                matrixCursor.addRow(new String[]{String.valueOf(NotificationSettingsHelper.canShowBadge(str3, null) ? 1 : 0)});
            }
            cursor = matrixCursor;
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
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
            j = openDB.insert("notifications", null, contentValues);
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
        if ("getKeyguardNotificationSortedKeys".equals(str)) {
            Bundle bundle2 = new Bundle();
            bundle2.putStringArrayList("sortedKeys", (ArrayList) ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).getSortedKeys());
            return bundle2;
        }
        String string = bundle.getString("package");
        String string2 = bundle.getString("channel_id");
        Log.d("NotificationProvider", String.format("call method=%s extras=%s", str, bundle.toString()));
        Bundle bundle3 = new Bundle();
        if ("getNotificationSettings".equals(str)) {
            bundle3.putBoolean("canShowBadge", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowBadge(getContext(), string));
            bundle3.putBoolean("canShowFloat", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canFloat(getContext(), string, string2));
            bundle3.putBoolean("canShowOnKeyguard", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowOnKeyguard(getContext(), string, string2));
            bundle3.putBoolean("canSound", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSound(getContext(), string));
            bundle3.putBoolean("canVibrate", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canVibrate(getContext(), string));
            bundle3.putBoolean("canLights", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canLights(getContext(), string));
        } else if ("getFoldImportance".equals(str)) {
            bundle3.putInt("foldImportance", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).getFoldImportance(getContext(), string));
        } else if ("canShowBadge".equals(str)) {
            bundle3.putBoolean("canShowBadge", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowBadge(getContext(), string));
        } else if ("canFloat".equals(str)) {
            bundle3.putBoolean("canShowFloat", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canFloat(getContext(), string, string2));
        } else if ("canShowOnKeyguard".equals(str)) {
            bundle3.putBoolean("canShowOnKeyguard", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowOnKeyguard(getContext(), string, string2));
        } else if ("canSound".equals(str)) {
            bundle3.putBoolean("canSound", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSound(getContext(), string));
        } else if ("canVibrate".equals(str)) {
            bundle3.putBoolean("canVibrate", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canVibrate(getContext(), string));
        } else if ("canLights".equals(str)) {
            bundle3.putBoolean("canLights", ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canLights(getContext(), string));
        } else if (NotificationUtil.isUidSystem(Binder.getCallingUid())) {
            if ("setFoldImportance".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setFoldImportance(getContext(), string, bundle.getInt("foldImportance", 0));
            } else if ("setShowBadge".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setShowBadge(getContext(), string, bundle.getBoolean("canShowBadge", false));
            } else if ("setFloat".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setFloat(getContext(), string, string2, bundle.getBoolean("canShowFloat", false));
            } else if ("setShowOnKeyguard".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setShowOnKeyguard(getContext(), string, string2, bundle.getBoolean("canShowOnKeyguard", false));
            } else if ("setSound".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setSound(getContext(), string, bundle.getBoolean("canSound", false));
            } else if ("setVibrate".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setVibrate(getContext(), string, bundle.getBoolean("canVibrate", false));
            } else if ("setLights".equals(str)) {
                ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setLights(getContext(), string, bundle.getBoolean("canLights", false));
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
