package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

public abstract class SecureSetting extends ContentObserver {
    private final Context mContext;
    private boolean mListening;
    private int mObservedValue;
    private final String mSettingName;
    private int mUserId;

    /* access modifiers changed from: protected */
    public abstract void handleValueChanged(int i, boolean z);

    protected SecureSetting(Context context, Handler handler, String str) {
        this(context, handler, str, ActivityManager.getCurrentUser());
    }

    public SecureSetting(Context context, Handler handler, String str, int i) {
        super(handler);
        this.mObservedValue = 0;
        this.mContext = context;
        this.mSettingName = str;
        this.mUserId = i;
    }

    public int getValue() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, 0, this.mUserId);
    }

    public void setValue(int i) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), this.mSettingName, i, this.mUserId);
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

    public String getKey() {
        return this.mSettingName;
    }
}
