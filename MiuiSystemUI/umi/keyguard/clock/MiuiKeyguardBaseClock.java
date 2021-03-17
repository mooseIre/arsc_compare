package com.android.keyguard.clock;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import java.util.Locale;

public class MiuiKeyguardBaseClock extends FrameLayout {
    protected boolean m24HourFormat;
    protected Context mContext = null;
    protected boolean mDarkStyle = false;
    protected int mDensityDpi;
    protected float mFontScale;
    protected String mLanguage;
    protected LayoutInflater mLayoutInflater;
    protected Resources mResources = null;
    private MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.clock.MiuiKeyguardBaseClock.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                MiuiKeyguardBaseClock.this.updateHourFormat();
                MiuiKeyguardBaseClock.this.onClockShowing();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            MiuiKeyguardBaseClock.this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
            MiuiKeyguardBaseClock.this.updateHourFormat();
            MiuiKeyguardBaseClock.this.onUserSwitch();
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onMagazineResourceInited() {
            MiuiKeyguardBaseClock.this.updateClockMagazineInfo();
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onRegionChanged() {
            MiuiKeyguardBaseClock.this.updateClockMagazineInfo();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            MiuiKeyguardBaseClock.this.updateClockMagazineInfo();
        }
    };
    protected int mUserId;

    public boolean hasOverlappingRendering() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onClockShowing() {
    }

    /* access modifiers changed from: protected */
    public void onUserSwitch() {
    }

    /* access modifiers changed from: protected */
    public void setSelectedClockPosition(int i) {
    }

    /* access modifiers changed from: protected */
    public void updateClockMagazineInfo() {
    }

    /* access modifiers changed from: protected */
    public void updateClockView(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void updateDrawableResources() {
    }

    /* access modifiers changed from: protected */
    public void updateLunarCalendarInfo() {
    }

    /* access modifiers changed from: protected */
    public void updateResidentTimeZone(String str) {
    }

    /* access modifiers changed from: protected */
    public void updateTime() {
    }

    /* access modifiers changed from: protected */
    public void updateTimeZone(String str) {
    }

    /* access modifiers changed from: protected */
    public void updateViewsLayoutParams() {
    }

    /* access modifiers changed from: protected */
    public void updateViewsTextSize() {
    }

    public MiuiKeyguardBaseClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mResources = this.mContext.getResources();
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mLanguage = Locale.getDefault().getLanguage();
        updateHourFormat();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void updateHourFormat() {
        this.m24HourFormat = DateFormat.is24HourFormat(this.mContext, this.mUserId);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setDarkStyle(this.mDarkStyle);
        updateViewsLayoutParams();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void setDarkStyle(boolean z) {
        this.mDarkStyle = z;
    }

    /* access modifiers changed from: protected */
    public int getClockHeight() {
        if (getHeight() > 0) {
            return getHeight();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        float f = configuration.fontScale;
        if (this.mFontScale != f) {
            updateViewsTextSize();
            this.mFontScale = f;
        }
        int i = configuration.densityDpi;
        if (this.mDensityDpi != i) {
            updateViewsTextSize();
            updateViewsLayoutParams();
            updateDrawableResources();
            this.mDensityDpi = i;
        }
        String language = configuration.locale.getLanguage();
        if (!TextUtils.isEmpty(language) && !language.equals(this.mLanguage)) {
            updateClockMagazineInfo();
            this.mLanguage = language;
            updateLunarCalendarInfo();
        }
    }

    /* access modifiers changed from: protected */
    public float getClockVisibleHeight() {
        if (getHeight() > 0) {
            return (float) getHeight();
        }
        return 0.0f;
    }
}
