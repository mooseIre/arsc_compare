package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.keyguard.CarrierText;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import miui.os.Build;

public class MiuiNotificationHeaderView extends MiuiHeaderView {
    private MiuiBatteryMeterView mBattery;
    private CarrierText mCarrierText;
    protected CommandQueue mCommandQueue;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    protected StatusBarIconController.MiuiLightDarkIconManager mIconManager;

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public void regionChanged() {
    }

    public MiuiNotificationHeaderView(Context context) {
        this(context, null);
    }

    public MiuiNotificationHeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiNotificationHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShortcutDestination = 1;
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateTimeColor();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.MiuiHeaderView
    public void onFinishInflate() {
        super.onFinishInflate();
        CarrierText carrierText = (CarrierText) findViewById(C0015R$id.carrier_text);
        this.mCarrierText = carrierText;
        if (carrierText != null && !Build.IS_INTERNATIONAL_BUILD) {
            carrierText.setVisibility(8);
        }
        this.mBattery = (MiuiBatteryMeterView) findViewById(C0015R$id.battery);
        NetworkSpeedView networkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mFullscreenNetworkSpeedView = networkSpeedView;
        networkSpeedView.setVisibilityByStatusBar(true);
        this.mIconManager = new StatusBarIconController.MiuiLightDarkIconManager((ViewGroup) findViewById(C0015R$id.statusIcons), this.mCommandQueue, true, ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone());
        updateTimeColor();
    }

    private void updateTimeColor() {
        ImageView imageView = this.mShortcut;
        if (imageView != null) {
            imageView.setImageDrawable(getResources().getDrawable(C0013R$drawable.notification_panel_manage_icon));
        }
        MiuiClock miuiClock = this.mClock;
        if (miuiClock != null) {
            miuiClock.setTextColor(getResources().getColor(C0011R$color.qs_control_header_clock_color));
        }
        MiuiClock miuiClock2 = this.mDateView;
        if (miuiClock2 != null) {
            miuiClock2.setTextColor(getResources().getColor(C0011R$color.qs_control_header_date_color));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
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
}
