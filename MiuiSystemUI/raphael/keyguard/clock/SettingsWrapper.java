package com.android.keyguard.clock;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsWrapper {
    private final ContentResolver mContentResolver;
    private final Migration mMigration;

    interface Migration {
        void migrate(String str, int i);
    }

    SettingsWrapper(ContentResolver contentResolver) {
        this(contentResolver, new Migrator(contentResolver));
    }

    @VisibleForTesting
    SettingsWrapper(ContentResolver contentResolver, Migration migration) {
        this.mContentResolver = contentResolver;
        this.mMigration = migration;
    }

    /* access modifiers changed from: package-private */
    public String getLockScreenCustomClockFace(int i) {
        return decode(Settings.Secure.getStringForUser(this.mContentResolver, "lock_screen_custom_clock_face", i), i);
    }

    /* access modifiers changed from: package-private */
    public String getDockedClockFace(int i) {
        return Settings.Secure.getStringForUser(this.mContentResolver, "docked_clock_face", i);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String decode(String str, int i) {
        if (str == null) {
            return str;
        }
        try {
            try {
                return new JSONObject(str).getString("clock");
            } catch (JSONException e) {
                Log.e("ClockFaceSettings", "JSON object does not contain clock field.", e);
                return null;
            }
        } catch (JSONException e2) {
            Log.e("ClockFaceSettings", "Settings value is not valid JSON", e2);
            this.mMigration.migrate(str, i);
            return str;
        }
    }

    private static final class Migrator implements Migration {
        private final ContentResolver mContentResolver;

        Migrator(ContentResolver contentResolver) {
            this.mContentResolver = contentResolver;
        }

        public void migrate(String str, int i) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("clock", str);
                Settings.Secure.putStringForUser(this.mContentResolver, "lock_screen_custom_clock_face", jSONObject.toString(), i);
            } catch (JSONException e) {
                Log.e("ClockFaceSettings", "Failed migrating settings value to JSON format", e);
            }
        }
    }
}
