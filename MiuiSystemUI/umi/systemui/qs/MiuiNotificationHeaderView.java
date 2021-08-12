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
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.phone.MiuiLightDarkIconManager;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import miui.os.Build;

public class MiuiNotificationHeaderView extends MiuiHeaderView {
    private MiuiBatteryMeterView mBattery;
    private CarrierText mCarrierText;
    protected CommandQueue mCommandQueue;
    private int mDisable2;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    protected MiuiLightDarkIconManager mIconManager;

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
        int i = this.mLastOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLastOrientation = i2;
        }
        updateTimeColor();
        setNormalHeight(0.0f);
        updateHeight();
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
        this.mIconManager = new MiuiLightDarkIconManager((ViewGroup) findViewById(C0015R$id.statusIcons), this.mCommandQueue, true, ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone());
        updateTimeColor();
        updateHeight();
    }

    private void updateHeight() {
        post(new Runnable() {
            /* class com.android.systemui.qs.$$Lambda$MiuiNotificationHeaderView$kMf2cGXZXYM32b4C6ygGCgqSW4I */

            public final void run() {
                MiuiNotificationHeaderView.this.lambda$updateHeight$0$MiuiNotificationHeaderView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateHeight$0 */
    public /* synthetic */ void lambda$updateHeight$0$MiuiNotificationHeaderView() {
        if (getNormalHeight() == 0.0f) {
            setNormalHeight((float) getHeight());
            setUnimportantHeight((float) getContext().getResources().getDimensionPixelSize(C0012R$dimen.unimportant_miui_header_height));
        }
        FoldManager.Companion.setNormalTarget(getNormalHeight());
        FoldManager.Companion.setUnimportantTarget(getUnimportantHeight());
        FoldManager.Companion.setHeaderDif(getNormalHeight() - getUnimportantHeight());
        if (FoldManager.Companion.isShowingUnimportant()) {
            showUnimportantWithoutAnim();
        }
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
    @Override // com.android.systemui.qs.MiuiHeaderView
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.MiuiHeaderView
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
        MiuiLightDarkIconManager miuiLightDarkIconManager = this.mIconManager;
        if (miuiLightDarkIconManager != null) {
            miuiLightDarkIconManager.setLight(!z, i);
        }
        MiuiBatteryMeterView miuiBatteryMeterView = this.mBattery;
        if (miuiBatteryMeterView != null) {
            miuiBatteryMeterView.onDarkChanged(rect, f, i, lightModeIconColorSingleTone, darkModeIconColorSingleTone, false);
        }
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public void disable(int i) {
        super.disable(i);
        if (this.mDisable2 != i) {
            this.mDisable2 = i;
            ImageView imageView = this.mShortcut;
            if (imageView != null) {
                imageView.setVisibility((i & 1) == 0 ? 0 : 8);
            }
        }
    }
}
