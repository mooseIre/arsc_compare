package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import miui.os.Build;

public class MiuiQSHeaderView extends MiuiHeaderView implements SuperSaveModeController.SuperSaveModeChangeListener {
    private MiuiBatteryMeterView mBattery;
    private CarrierText mCarrierText;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    private StatusBarIconController.MiuiLightDarkIconManager mIconManager;
    private LinearLayout mStatusIcons;
    private boolean mSuperSave;

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
    }

    public MiuiQSHeaderView(Context context) {
        this(context, null);
    }

    public MiuiQSHeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiQSHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.MiuiHeaderView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCarrierText = (CarrierText) findViewById(C0015R$id.notification_shade_carrier);
        NetworkSpeedView networkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mFullscreenNetworkSpeedView = networkSpeedView;
        networkSpeedView.setVisibilityByStatusBar(true);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0015R$id.statusIcons);
        this.mStatusIcons = linearLayout;
        this.mIconManager = new StatusBarIconController.MiuiLightDarkIconManager(linearLayout, (CommandQueue) Dependency.get(CommandQueue.class), true, ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone());
        this.mBattery = (MiuiBatteryMeterView) findViewById(C0015R$id.battery);
        this.mLastOrientation = getResources().getConfiguration().orientation;
        updateLayout();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).removeCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = this.mLastOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLastOrientation = i2;
            updateLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void updateLayout() {
        boolean showCarrier = showCarrier();
        if (this.mLastOrientation == 1) {
            this.mClock.setClockVisibilityByController(true);
            this.mDateView.setClockMode(1);
            updateShortCutVisibility();
            if (showCarrier) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
                layoutParams.removeRule(6);
                layoutParams.removeRule(12);
                layoutParams.addRule(2, C0015R$id.notification_shade_carrier);
                this.mDateView.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mCarrierText.getLayoutParams();
                layoutParams2.removeRule(17);
                layoutParams2.removeRule(6);
                layoutParams2.addRule(20);
                layoutParams2.addRule(12);
                layoutParams2.setMarginStart(0);
                this.mCarrierText.setLayoutParams(layoutParams2);
                this.mCarrierText.setVisibility(0);
                return;
            }
            this.mCarrierText.setVisibility(8);
            RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
            layoutParams3.removeRule(2);
            layoutParams3.addRule(6, C0015R$id.system_icon_area);
            layoutParams3.addRule(12);
            this.mDateView.setLayoutParams(layoutParams3);
            return;
        }
        this.mClock.setClockVisibilityByController(false);
        this.mDateView.setClockMode(2);
        updateShortCutVisibility();
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
        layoutParams4.removeRule(2);
        layoutParams4.addRule(6, C0015R$id.system_icon_area);
        layoutParams4.addRule(12);
        this.mDateView.setLayoutParams(layoutParams4);
        if (showCarrier) {
            RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mCarrierText.getLayoutParams();
            layoutParams5.removeRule(20);
            layoutParams5.addRule(6, C0015R$id.system_icon_area);
            layoutParams5.addRule(12);
            layoutParams5.addRule(17, C0015R$id.date_time);
            layoutParams5.setMarginStart(getResources().getDimensionPixelOffset(C0012R$dimen.notch_expanded_header_carrier_margin));
            this.mCarrierText.setLayoutParams(layoutParams5);
            this.mCarrierText.setVisibility(0);
            return;
        }
        this.mCarrierText.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public boolean showCarrier() {
        if (Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CM_CUSTOMIZATION_TEST || "TW".equalsIgnoreCase(SystemProperties.get("ro.miui.region", ""))) {
            return true;
        }
        return ((RelativeLayout) this).mContext.getResources().getBoolean(C0010R$bool.show_carrier_in_status_bar_header);
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public void themeChanged() {
        boolean z = getResources().getBoolean(C0010R$bool.expanded_status_bar_darkmode);
        Rect rect = new Rect(0, 0, 0, 0);
        float f = z ? 1.0f : 0.0f;
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        int lightModeIconColorSingleTone = darkIconDispatcher.getLightModeIconColorSingleTone();
        int darkModeIconColorSingleTone = darkIconDispatcher.getDarkModeIconColorSingleTone();
        int i = z ? darkModeIconColorSingleTone : lightModeIconColorSingleTone;
        MiuiClock miuiClock = this.mDateView;
        if (miuiClock != null) {
            miuiClock.onDarkChanged(rect, f, i, lightModeIconColorSingleTone, darkModeIconColorSingleTone, false);
        }
        MiuiClock miuiClock2 = this.mClock;
        if (miuiClock2 != null) {
            miuiClock2.onDarkChanged(rect, f, i, lightModeIconColorSingleTone, darkModeIconColorSingleTone, false);
        }
        NetworkSpeedView networkSpeedView = this.mFullscreenNetworkSpeedView;
        if (networkSpeedView != null) {
            networkSpeedView.onDarkChanged(rect, f, i, lightModeIconColorSingleTone, darkModeIconColorSingleTone, false);
        }
        CarrierText carrierText = this.mCarrierText;
        if (carrierText != null) {
            carrierText.setTextColor(i);
        }
        StatusBarIconController.MiuiLightDarkIconManager miuiLightDarkIconManager = this.mIconManager;
        if (miuiLightDarkIconManager != null) {
            miuiLightDarkIconManager.setLight(!z, i);
        }
        MiuiBatteryMeterView miuiBatteryMeterView = this.mBattery;
        if (miuiBatteryMeterView != null) {
            miuiBatteryMeterView.onDarkChanged(rect, f, i, lightModeIconColorSingleTone, darkModeIconColorSingleTone, false);
        }
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public void regionChanged() {
        updateLayout();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return isEnabled() && super.onInterceptTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        super.dispatchTouchEvent(motionEvent);
        return isEnabled();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mDateView.setEnabled(z);
    }

    @Override // com.android.systemui.controlcenter.policy.SuperSaveModeController.SuperSaveModeChangeListener
    public void onSuperSaveModeChange(boolean z) {
        this.mSuperSave = z;
        updateShortCutVisibility();
    }

    private void updateShortCutVisibility() {
        ImageView imageView = this.mShortcut;
        if (imageView != null) {
            imageView.setVisibility((this.mLastOrientation != 1 || this.mSuperSave) ? 8 : 0);
        }
    }
}
