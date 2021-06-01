package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import miui.keyguard.clock.MiuiBaseClock;
import miui.system.R;

public class MiuiKeyguardLeftTopLargeClock extends MiuiKeyguardSingleClock {
    private MiuiBaseClock mMiuiNoticationStateClock;

    @Override // com.android.keyguard.clock.MiuiKeyguardBaseClock
    public boolean hasOverlappingRendering() {
        return false;
    }

    public MiuiKeyguardLeftTopLargeClock(Context context) {
        this(context, null);
    }

    public MiuiKeyguardLeftTopLargeClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        boolean z = context.getResources().getBoolean(C0010R$bool.config_keyguard_clock_notification_center);
        try {
            this.mMiuiBaseClock = this.mLayoutInflater.inflate(R.layout.miui_left_top_large_clock, (ViewGroup) null, false);
            updateLunarCalendarInfo();
            if (z) {
                this.mMiuiNoticationStateClock = this.mLayoutInflater.inflate(R.layout.miui_center_horizontal_clock, (ViewGroup) null, false);
            } else {
                this.mMiuiNoticationStateClock = this.mLayoutInflater.inflate(R.layout.miui_left_top_clock, (ViewGroup) null, false);
            }
            this.mMiuiNoticationStateClock.setShowLunarCalendar(false);
            this.mMiuiNoticationStateClock.setAlpha(0.0f);
            this.mMiuiNoticationStateClock.setVisibility(8);
        } catch (Exception e) {
            Log.e("MiuiKeyguardLeftTopLargeClock", "init clock exception", e);
        }
        this.mClockContainer.addView(this.mMiuiBaseClock);
        this.mClockContainer.addView(this.mMiuiNoticationStateClock);
        this.mMagazineClockView.updateViewsForClockPosition(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimOutEnd() {
        this.mMiuiBaseClock.setVisibility(8);
        this.mMiuiNoticationStateClock.setAlpha(0.0f);
        this.mMiuiNoticationStateClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimOutUpdate(float f) {
        this.mMiuiBaseClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNotificationStateAnimInUpdate(float f) {
        this.mMiuiNoticationStateClock.setAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimOutEnd() {
        this.mMiuiNoticationStateClock.setVisibility(8);
        this.mMiuiBaseClock.setAlpha(0.0f);
        this.mMiuiBaseClock.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimOutUpdate(float f) {
        this.mMiuiNoticationStateClock.setClockAlpha(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock
    public void toNormalStateAnimInUpdate(float f) {
        this.mMiuiBaseClock.setAlpha(f);
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void setDarkStyle(boolean z) {
        if (z != this.mDarkStyle) {
            super.setDarkStyle(z);
            this.mMiuiBaseClock.setTextColorDark(z);
            this.mMiuiNoticationStateClock.setTextColorDark(z);
            int color = z ? getContext().getResources().getColor(C0011R$color.miui_common_unlock_screen_common_time_dark_text_color) : -1;
            this.mOwnerInfo.setTextColor(color);
            this.mMagazineClockView.setTextColor(color);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateHourFormat() {
        super.updateHourFormat();
        MiuiBaseClock miuiBaseClock = this.mMiuiNoticationStateClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.setIs24HourFormat(this.m24HourFormat);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTimeZone(String str) {
        super.updateTimeZone(str);
        MiuiBaseClock miuiBaseClock = this.mMiuiNoticationStateClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.updateTimeZone(str);
        }
    }

    @Override // com.android.keyguard.clock.MiuiKeyguardSingleClock, com.android.keyguard.clock.MiuiKeyguardBaseClock
    public void updateTime() {
        super.updateTime();
        MiuiBaseClock miuiBaseClock = this.mMiuiNoticationStateClock;
        if (miuiBaseClock != null) {
            miuiBaseClock.updateTime();
        }
    }
}
