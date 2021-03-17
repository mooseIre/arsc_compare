package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

public abstract class GlobalSetting extends ContentObserver {
    private final Context mContext;
    private final String mSettingName;

    /* access modifiers changed from: protected */
    public abstract void handleValueChanged(int i);

    public GlobalSetting(Context context, Handler handler, String str) {
        super(handler);
        this.mContext = context;
        this.mSettingName = str;
    }

    public int getValue() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), this.mSettingName, 0);
    }

    public void setListening(boolean z) {
        if (z) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(this.mSettingName), false, this);
        } else {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public void onChange(boolean z) {
        handleValueChanged(getValue());
    }
}
