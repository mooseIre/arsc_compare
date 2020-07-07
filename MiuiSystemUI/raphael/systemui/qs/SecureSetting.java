package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.systemui.statusbar.policy.Listenable;

public abstract class SecureSetting extends ContentObserver implements Listenable {
    private final Context mContext;
    private boolean mListening;
    private int mObservedValue = 0;
    private final String mSettingName;
    private int mUserId;

    /* access modifiers changed from: protected */
    public abstract void handleValueChanged(int i, boolean z);

    public SecureSetting(Context context, Handler handler, String str) {
        super(handler);
        this.mContext = context;
        this.mSettingName = str;
        this.mUserId = ActivityManager.getCurrentUser();
    }

    public int getValue() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, 0, this.mUserId);
    }

    public void setListening(boolean z) {
        if (z != this.mListening) {
            this.mListening = z;
            if (z) {
                this.mObservedValue = getValue();
                this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(this.mSettingName), false, this, this.mUserId);
                return;
            }
            this.mContext.getContentResolver().unregisterContentObserver(this);
            this.mObservedValue = 0;
        }
    }

    public void onChange(boolean z) {
        int value = getValue();
        handleValueChanged(value, value != this.mObservedValue);
        this.mObservedValue = value;
    }

    public void setUserId(int i) {
        this.mUserId = i;
        if (this.mListening) {
            setListening(false);
            setListening(true);
        }
    }
}
