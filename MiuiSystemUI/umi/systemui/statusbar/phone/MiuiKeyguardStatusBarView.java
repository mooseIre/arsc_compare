package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.RegionController;

public class MiuiKeyguardStatusBarView extends KeyguardStatusBarView implements RegionController.Callback {
    private CurrentUserTracker mCurrentUserTracker = new CurrentUserTracker((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class)) {
        public void onUserSwitched(int i) {
            MiuiKeyguardStatusBarView.this.mShowCarrierObserver.onChange(false);
        }
    };
    private boolean mDark = false;
    private boolean mLeftHoleDevice;
    /* access modifiers changed from: private */
    public boolean mShowCarrier;
    /* access modifiers changed from: private */
    public ContentObserver mShowCarrierObserver = new ContentObserver((Handler) Dependency.get(Dependency.MAIN_HANDLER)) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiKeyguardStatusBarView miuiKeyguardStatusBarView = MiuiKeyguardStatusBarView.this;
            boolean z2 = true;
            if (Settings.System.getIntForUser(miuiKeyguardStatusBarView.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, -2) != 1) {
                z2 = false;
            }
            boolean unused = miuiKeyguardStatusBarView.mShowCarrier = z2;
            Log.d("MiuiKeyguardStatusBarView", "onChange: mShowCarrier = " + MiuiKeyguardStatusBarView.this.mShowCarrier);
            MiuiKeyguardStatusBarView.this.updateCarrierVisibility();
        }
    };
    private boolean mTWRegion;

    public MiuiKeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLeftHoleDevice = context.getResources().getBoolean(C0010R$bool.left_hole_device);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCurrentUserTracker.startTracking();
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        this.mShowCarrierObserver.onChange(false);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        this.mCurrentUserTracker.stopTracking();
    }

    /* access modifiers changed from: protected */
    public void updateIconsAndTextColors() {
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        int lightModeIconColorSingleTone = darkIconDispatcher.getLightModeIconColorSingleTone();
        int darkModeIconColorSingleTone = darkIconDispatcher.getDarkModeIconColorSingleTone();
        boolean z = this.mDark;
        int i = z ? darkModeIconColorSingleTone : lightModeIconColorSingleTone;
        float f = z ? 1.0f : 0.0f;
        Log.d("MiuiKeyguardStatusBarView", "updateIconsAndTextColors: dark = " + z + ", iconColor = " + i + ", intensity = " + f);
        this.mCarrierLabel.setTextColor(i);
        StatusBarIconController.MiuiLightDarkIconManager miuiLightDarkIconManager = this.mIconManager;
        if (miuiLightDarkIconManager != null) {
            miuiLightDarkIconManager.setLight(!z, i);
        }
        StatusBarIconController.MiuiLightDarkIconManager miuiLightDarkIconManager2 = this.mDripLeftIconManager;
        if (miuiLightDarkIconManager2 != null) {
            miuiLightDarkIconManager2.setLight(!z, i);
        }
        StatusBarIconController.MiuiLightDarkIconManager miuiLightDarkIconManager3 = this.mDripRightIconManager;
        if (miuiLightDarkIconManager3 != null) {
            miuiLightDarkIconManager3.setLight(!z, i);
        }
        float f2 = f;
        int i2 = i;
        int i3 = lightModeIconColorSingleTone;
        int i4 = darkModeIconColorSingleTone;
        applyDarkness(C0015R$id.fullscreen_network_speed_view, this.mEmptyRect, f2, i2, i3, i4);
        applyDarkness(C0015R$id.battery, this.mEmptyRect, f2, i2, i3, i4);
        applyDarkness(C0015R$id.clock, this.mEmptyRect, f2, i2, i3, i4);
    }

    /* access modifiers changed from: private */
    public void updateCarrierVisibility() {
        this.mCarrierLabel.setVisibility(((this.mTWRegion || !this.mLeftHoleDevice) && this.mShowCarrier) ? 0 : 8);
    }

    private void applyDarkness(int i, Rect rect, float f, int i2, int i3, int i4) {
        View findViewById = findViewById(i);
        if (findViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) findViewById).onDarkChanged(rect, f, i2, i3, i4, false);
        }
    }

    public void setDarkStyle(boolean z) {
        super.setDarkStyle(z);
        this.mDark = z;
        updateIconsAndTextColors();
    }

    public void onRegionChanged(String str) {
        this.mTWRegion = "TW".equals(str);
        updateCarrierVisibility();
    }
}
