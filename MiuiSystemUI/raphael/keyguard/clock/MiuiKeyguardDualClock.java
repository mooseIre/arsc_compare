package com.android.keyguard.clock;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import miui.keyguard.clock.MiuiDualClock;
import miui.system.R;

public class MiuiKeyguardDualClock extends MiuiKeyguardBaseClock {
    MiuiDualClock.OnLocalCityChangeListener mLocalCityChangeListener;
    private MiuiDualClock mMiuiDualClock;

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onUserSwitch() {
    }

    public MiuiKeyguardDualClock(Context context) {
        this(context, null);
    }

    public MiuiKeyguardDualClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLocalCityChangeListener = new MiuiDualClock.OnLocalCityChangeListener() {
            /* class com.android.keyguard.clock.MiuiKeyguardDualClock.AnonymousClass1 */

            public void onLocalCityChanged(String str) {
                Settings.System.putString(MiuiKeyguardDualClock.this.mContext.getContentResolver(), "local_city", str);
            }
        };
        try {
            MiuiDualClock inflate = this.mLayoutInflater.inflate(R.layout.miui_dual_clock, (ViewGroup) null, false);
            this.mMiuiDualClock = inflate;
            inflate.setOnLocalCityChangeListener(this.mLocalCityChangeListener);
        } catch (Exception e) {
            Log.e("MiuiKeyguardDualClock", "init clock exception", e);
        }
        addView(this.mMiuiDualClock);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void setDarkStyle(boolean z) {
        super.setDarkStyle(z);
        if (this.mDarkStyle != z) {
            this.mMiuiDualClock.setTextColorDark(z);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiDualClock miuiDualClock = this.mMiuiDualClock;
        if (miuiDualClock != null) {
            miuiDualClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateResidentTimeZone(String str) {
        this.mMiuiDualClock.updateResidentTimeZone(str);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void onClockShowing() {
        MiuiDualClock miuiDualClock = this.mMiuiDualClock;
        if (miuiDualClock != null) {
            miuiDualClock.updateTime();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTimeZone(String str) {
        this.mMiuiDualClock.updateTimeZone(str);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTime() {
        this.mMiuiDualClock.updateTime();
    }
}
