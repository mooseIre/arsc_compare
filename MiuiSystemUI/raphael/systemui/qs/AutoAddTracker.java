package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Prefs;
import com.android.systemui.util.UserAwareController;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AutoAddTracker implements UserAwareController {
    private static final String[][] CONVERT_PREFS = {new String[]{"QsHotspotAdded", "hotspot"}, new String[]{"QsDataSaverAdded", "saver"}, new String[]{"QsInvertColorsAdded", "inversion"}, new String[]{"QsWorkAdded", "work"}, new String[]{"QsNightDisplayAdded", "night"}};
    /* access modifiers changed from: private */
    public final ArraySet<String> mAutoAdded;
    private final Context mContext;
    @VisibleForTesting
    protected final ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            AutoAddTracker.this.mAutoAdded.clear();
            AutoAddTracker.this.mAutoAdded.addAll(AutoAddTracker.this.getAdded());
        }
    };
    private int mUserId;

    public AutoAddTracker(Context context, int i) {
        this.mContext = context;
        this.mUserId = i;
        this.mAutoAdded = new ArraySet<>(getAdded());
        if (this.mUserId == 0) {
            for (String[] strArr : CONVERT_PREFS) {
                if (Prefs.getBoolean(context, strArr[0], false)) {
                    setTileAdded(strArr[1]);
                    Prefs.remove(context, strArr[0]);
                }
            }
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("qs_auto_tiles"), false, this.mObserver, -1);
    }

    public void changeUser(UserHandle userHandle) {
        if (userHandle.getIdentifier() != this.mUserId) {
            this.mUserId = userHandle.getIdentifier();
            this.mAutoAdded.clear();
            this.mAutoAdded.addAll(getAdded());
        }
    }

    public int getCurrentUserId() {
        return this.mUserId;
    }

    public boolean isAdded(String str) {
        return this.mAutoAdded.contains(str);
    }

    public void setTileAdded(String str) {
        if (this.mAutoAdded.add(str)) {
            saveTiles();
        }
    }

    public void setTileRemoved(String str) {
        if (this.mAutoAdded.remove(str)) {
            saveTiles();
        }
    }

    private void saveTiles() {
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "qs_auto_tiles", TextUtils.join(",", this.mAutoAdded), this.mUserId);
    }

    /* access modifiers changed from: private */
    public Collection<String> getAdded() {
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "qs_auto_tiles", this.mUserId);
        if (stringForUser == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(stringForUser.split(","));
    }

    public static class Builder {
        private final Context mContext;
        private int mUserId;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setUserId(int i) {
            this.mUserId = i;
            return this;
        }

        public AutoAddTracker build() {
            return new AutoAddTracker(this.mContext, this.mUserId);
        }
    }
}
