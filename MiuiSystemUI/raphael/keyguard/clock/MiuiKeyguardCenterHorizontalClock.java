package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import miui.system.R;

public class MiuiKeyguardCenterHorizontalClock extends MiuiKeyguardSingleClock {
    public MiuiKeyguardCenterHorizontalClock(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardCenterHorizontalClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        try {
            this.mMiuiBaseClock = this.mLayoutInflater.inflate(R.layout.miui_center_horizontal_clock, (ViewGroup) null, false);
            updateLunarCalendarInfo();
        } catch (Exception e) {
            Log.e("MiuiKeyguardCenterHorizontalClock", "init clock exception", e);
        }
        this.mClockContainer.addView(this.mMiuiBaseClock);
        this.mMagazineClockView.updateViewsForClockPosition(false);
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowSwitchAnim() {
        return this.mShowLunarCalendar || this.mShowOwnerInfo;
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutEnd() {
        this.mMiuiBaseClock.getLunarCalendarView().setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutUpdate(float f) {
        this.mMiuiBaseClock.getLunarCalendarView().setAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutEnd() {
        this.mMiuiBaseClock.getLunarCalendarView().setAlpha(0.0f);
        updateLunarCalendarInfo();
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimInUpdate(float f) {
        this.mMiuiBaseClock.getLunarCalendarView().setAlpha(f);
    }

    public void setDarkStyle(boolean z) {
        if (z != this.mDarkStyle) {
            super.setDarkStyle(z);
            this.mMiuiBaseClock.setTextColorDark(z);
        }
    }
}
