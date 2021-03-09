package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.DualClockObserver;
import com.android.systemui.statusbar.policy.RegionController;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;
import java.util.ArrayList;
import java.util.Arrays;

public class MiuiKeyguardStatusBarView extends KeyguardStatusBarView implements RegionController.Callback, DualClockObserver.Callback, ForceBlackObserver.Callback {
    private CurrentUserTracker mCurrentUserTracker = new CurrentUserTracker((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class)) {
        public void onUserSwitched(int i) {
            MiuiKeyguardStatusBarView.this.mShowCarrierObserver.onChange(false);
        }
    };
    private boolean mDark = false;
    private boolean mForceBlack = false;
    private boolean mIsShowDualClock = false;
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
    private boolean mShowCarrierUnderLeftHoleKeyguard;
    private boolean mTWRegion;

    public MiuiKeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLeftHoleDevice = context.getResources().getBoolean(C0010R$bool.left_hole_device);
        this.mShowCarrierUnderLeftHoleKeyguard = context.getResources().getBoolean(C0010R$bool.show_carrier_under_left_hole_keyguard);
    }

    public void onDensityOrFontScaleChanged() {
        super.onDensityOrFontScaleChanged();
        updateViewStatusBarPaddingTop(this.mStatusIconArea);
        updateViewStatusBarPaddingTop(this.mStatusBarPromptContainer);
        updateViewStatusBarPaddingTop(this.mDripLeftStatusIconFrameContainer);
        updateViewStatusBarPaddingTop(this.mCarrierLabel);
        updateTextViewClockSize(this.mCarrierLabel);
        updateTextViewClockSize(this.mNetworkSpeedView);
    }

    public void onMiuiThemeChanged(boolean z) {
        Class cls = MiuiDripLeftStatusBarIconControllerImpl.class;
        Class cls2 = StatusBarIconController.class;
        if (this.mIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls2)).removeIconGroup(this.mIconManager);
            ((StatusBarIconController) Dependency.get(cls2)).addIconGroup(this.mIconManager);
        }
        if (this.mDripLeftIconManager != null) {
            ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(cls)).removeIconGroup(this.mDripLeftIconManager);
            ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(cls)).addIconGroup(this.mDripLeftIconManager);
        }
        if (this.mDripRightIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls2)).removeIconGroup(this.mDripRightIconManager);
            ((StatusBarIconController) Dependency.get(cls2)).addIconGroup(this.mDripRightIconManager, new ArrayList(Arrays.asList(getContext().getResources().getStringArray(C0008R$array.config_drip_right_block_statusBarIcons))));
        }
        updateIconsAndTextColors();
    }

    /* access modifiers changed from: protected */
    public void updateViewStatusBarPaddingTop(View view) {
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), getResources().getDimensionPixelSize(C0012R$dimen.status_bar_padding_top), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /* access modifiers changed from: protected */
    public void updateTextViewClockSize(TextView textView) {
        if (textView != null) {
            textView.setTextSize(0, (float) getContext().getResources().getDimensionPixelSize(C0012R$dimen.status_bar_clock_size));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        Class cls = ForceBlackObserver.class;
        super.onAttachedToWindow();
        this.mCurrentUserTracker.startTracking();
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
        ((DualClockObserver) Dependency.get(DualClockObserver.class)).addCallback(this);
        ((ForceBlackObserver) Dependency.get(cls)).addCallback(this);
        this.mForceBlack = ((ForceBlackObserver) Dependency.get(cls)).isForceBlack();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        this.mShowCarrierObserver.onChange(false);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).removeCallback(this);
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        ((DualClockObserver) Dependency.get(DualClockObserver.class)).removeCallback(this);
        this.mCurrentUserTracker.stopTracking();
    }

    /* access modifiers changed from: protected */
    public void updateIconsAndTextColors() {
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        int lightModeIconColorSingleTone = darkIconDispatcher.getLightModeIconColorSingleTone();
        int darkModeIconColorSingleTone = darkIconDispatcher.getDarkModeIconColorSingleTone();
        boolean z = !this.mForceBlack && this.mDark;
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
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0006, code lost:
        r1 = r3.mLeftHoleDevice;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateCarrierVisibility() {
        /*
            r3 = this;
            android.widget.TextView r0 = r3.mCarrierLabel
            boolean r1 = r3.mTWRegion
            if (r1 != 0) goto L_0x0014
            boolean r1 = r3.mLeftHoleDevice
            if (r1 == 0) goto L_0x0014
            boolean r2 = r3.mShowCarrierUnderLeftHoleKeyguard
            if (r2 != 0) goto L_0x0014
            if (r1 == 0) goto L_0x001a
            boolean r1 = r3.mIsShowDualClock
            if (r1 == 0) goto L_0x001a
        L_0x0014:
            boolean r3 = r3.mShowCarrier
            if (r3 == 0) goto L_0x001a
            r3 = 0
            goto L_0x001c
        L_0x001a:
            r3 = 8
        L_0x001c:
            r0.setVisibility(r3)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView.updateCarrierVisibility():void");
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

    public void onDualShowClockChanged(boolean z) {
        this.mIsShowDualClock = z;
        updateCarrierVisibility();
    }

    public void onForceBlackChange(boolean z, boolean z2) {
        this.mForceBlack = z;
        updateIconsAndTextColors();
    }
}
