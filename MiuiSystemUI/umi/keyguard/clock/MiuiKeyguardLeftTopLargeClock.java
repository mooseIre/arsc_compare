package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import com.android.systemui.C0011R$color;
import miui.keyguard.clock.MiuiLeftTopClock;
import miui.system.R;

public class MiuiKeyguardLeftTopLargeClock extends MiuiKeyguardSingleClock {
    private MiuiLeftTopClock mLeftTopClock;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public MiuiKeyguardLeftTopLargeClock(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardLeftTopLargeClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        try {
            this.mMiuiBaseClock = this.mLayoutInflater.inflate(R.layout.miui_left_top_large_clock, (ViewGroup) null, false);
            updateLunarCalendarInfo();
            MiuiLeftTopClock inflate = this.mLayoutInflater.inflate(R.layout.miui_left_top_clock, (ViewGroup) null, false);
            this.mLeftTopClock = inflate;
            inflate.setShowLunarCalendar(false);
            this.mLeftTopClock.setAlpha(0.0f);
            this.mLeftTopClock.setVisibility(8);
        } catch (Exception e) {
            Log.e("MiuiKeyguardLeftTopLargeClock", "init clock exception", e);
        }
        this.mClockContainer.addView(this.mMiuiBaseClock);
        this.mClockContainer.addView(this.mLeftTopClock);
        this.mMagazineClockView.updateViewsForClockPosition(true);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutEnd() {
        this.mMiuiBaseClock.setVisibility(8);
        this.mLeftTopClock.setAlpha(0.0f);
        this.mLeftTopClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimOutUpdate(float f) {
        this.mMiuiBaseClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNotificationStateAnimInUpdate(float f) {
        this.mLeftTopClock.setAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutEnd() {
        this.mLeftTopClock.setVisibility(8);
        this.mMiuiBaseClock.setAlpha(0.0f);
        this.mMiuiBaseClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimOutUpdate(float f) {
        this.mLeftTopClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    public void toNormalStateAnimInUpdate(float f) {
        this.mMiuiBaseClock.setAlpha(f);
    }

    public void setDarkStyle(boolean z) {
        if (z != this.mDarkStyle) {
            super.setDarkStyle(z);
            this.mMiuiBaseClock.setTextColorDark(z);
            this.mLeftTopClock.setTextColorDark(z);
            int color = z ? getContext().getResources().getColor(C0011R$color.miui_common_unlock_screen_common_time_dark_text_color) : -1;
            this.mOwnerInfo.setTextColor(color);
            this.mMagazineClockView.setTextColor(color);
        }
    }

    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiLeftTopClock miuiLeftTopClock = this.mLeftTopClock;
        if (miuiLeftTopClock != null) {
            miuiLeftTopClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    public void updateTimeZone(String str) {
        super.updateTimeZone(str);
        MiuiLeftTopClock miuiLeftTopClock = this.mLeftTopClock;
        if (miuiLeftTopClock != null) {
            miuiLeftTopClock.updateTimeZone(str);
        }
    }

    public void updateTime() {
        super.updateTime();
        MiuiLeftTopClock miuiLeftTopClock = this.mLeftTopClock;
        if (miuiLeftTopClock != null) {
            miuiLeftTopClock.updateTime();
        }
    }
}
