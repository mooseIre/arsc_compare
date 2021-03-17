package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import miui.keyguard.clock.MiuiCenterHorizontalClock;
import miui.system.R;

public class MiuiKeyguardCenterVerticalClock extends MiuiKeyguardSingleClock {
    private MiuiCenterHorizontalClock mMiuiCenterHorizontalClock;

    public MiuiKeyguardCenterVerticalClock(Context context) {
        this(context, null);
    }

    public MiuiKeyguardCenterVerticalClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        try {
            this.mMiuiBaseClock = this.mLayoutInflater.inflate(R.layout.miui_vertical_clock, (ViewGroup) null, false);
            updateLunarCalendarInfo();
            MiuiCenterHorizontalClock inflate = this.mLayoutInflater.inflate(R.layout.miui_center_horizontal_clock, (ViewGroup) null, false);
            this.mMiuiCenterHorizontalClock = inflate;
            inflate.setShowLunarCalendar(false);
            this.mMiuiCenterHorizontalClock.setAlpha(0.0f);
            this.mMiuiCenterHorizontalClock.setVisibility(8);
        } catch (Exception e) {
            Log.e("MiuiKeyguardCenterVerticalClock", "init clock exception", e);
        }
        this.mClockContainer.addView(this.mMiuiBaseClock);
        this.mClockContainer.addView(this.mMiuiCenterHorizontalClock);
        this.mMagazineClockView.updateViewsForClockPosition(false);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void setDarkStyle(boolean z) {
        if (z != this.mDarkStyle) {
            super.setDarkStyle(z);
            this.mMiuiBaseClock.setTextColorDark(z);
            this.mMiuiCenterHorizontalClock.setTextColorDark(z);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimOutEnd() {
        this.mMiuiBaseClock.setVisibility(8);
        this.mMiuiCenterHorizontalClock.setAlpha(0.0f);
        this.mMiuiCenterHorizontalClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimOutUpdate(float f) {
        this.mMiuiBaseClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimInUpdate(float f) {
        this.mMiuiCenterHorizontalClock.setAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimOutEnd() {
        this.mMiuiCenterHorizontalClock.setVisibility(8);
        this.mMiuiBaseClock.setAlpha(0.0f);
        this.mMiuiBaseClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimOutUpdate(float f) {
        this.mMiuiCenterHorizontalClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimInUpdate(float f) {
        this.mMiuiBaseClock.setAlpha(f);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTimeZone(String str) {
        super.updateTimeZone(str);
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.updateTimeZone(str);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTime() {
        super.updateTime();
        MiuiCenterHorizontalClock miuiCenterHorizontalClock = this.mMiuiCenterHorizontalClock;
        if (miuiCenterHorizontalClock != null) {
            miuiCenterHorizontalClock.updateTime();
        }
    }
}
