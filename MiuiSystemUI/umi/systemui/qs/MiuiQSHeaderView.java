package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import codeinjection.CodeInjection;
import com.android.keyguard.CarrierText;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.phone.MiuiLightDarkIconManager;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import miui.os.Build;

public class MiuiQSHeaderView extends MiuiHeaderView implements SuperSaveModeController.SuperSaveModeChangeListener {
    private MiuiBatteryMeterView mBattery;
    private CarrierText mCarrierText;
    private int mDisable2;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    private MiuiLightDarkIconManager mIconManager;
    private float mNormalHeightLandscape;
    private LinearLayout mStatusIcons;
    private boolean mSuperSave;
    private float mUnimportantHeightLandscape;

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
        this.mIconManager = new MiuiLightDarkIconManager(linearLayout, (CommandQueue) Dependency.get(CommandQueue.class), true, ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone());
        this.mBattery = (MiuiBatteryMeterView) findViewById(C0015R$id.battery);
        this.mLastOrientation = getResources().getConfiguration().orientation;
        updateLayout();
        updateHeight();
    }

    private void updateHeight() {
        post(new Runnable() {
            /* class com.android.systemui.qs.$$Lambda$MiuiQSHeaderView$FJsLk97tMocNR4cViAP_V_hWlic */

            public final void run() {
                MiuiQSHeaderView.this.lambda$updateHeight$0$MiuiQSHeaderView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateHeight$0 */
    public /* synthetic */ void lambda$updateHeight$0$MiuiQSHeaderView() {
        int i;
        boolean isShowingUnimportant = FoldManager.Companion.isShowingUnimportant();
        if (showCarrier()) {
            i = getContext().getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height_with_carrier);
        } else {
            i = getContext().getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height);
        }
        setNormalHeight((float) i);
        setUnimportantHeight((float) getContext().getResources().getDimensionPixelSize(C0012R$dimen.unimportant_miui_header_height));
        int dimensionPixelSize = getContext().getResources().getDimensionPixelSize(C0012R$dimen.unimportant_miui_qs_dif);
        this.mNormalHeightLandscape = (float) getContext().getResources().getDimensionPixelSize(17105495);
        this.mUnimportantHeightLandscape = super.getUnimportantHeight();
        FoldManager.Companion.setNormalTarget(getNormalHeight());
        FoldManager.Companion.setUnimportantTarget(getUnimportantHeight());
        FoldManager.Companion.setHeaderDif((getNormalHeight() + ((float) dimensionPixelSize)) - getUnimportantHeight());
        initFolme();
        if (isShowingUnimportant) {
            showUnimportantWithoutAnim();
        }
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public float getNormalHeight() {
        if (this.mLastOrientation == 2) {
            return this.mNormalHeightLandscape;
        }
        return super.getNormalHeight();
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public float getUnimportantHeight() {
        if (this.mLastOrientation == 2) {
            return this.mUnimportantHeightLandscape;
        }
        return super.getUnimportantHeight();
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
        } else {
            setNormalHeight(0.0f);
            this.mNormalHeightLandscape = 0.0f;
        }
        updateHeight();
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
        if (Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CM_CUSTOMIZATION_TEST || "TW".equalsIgnoreCase(SystemProperties.get("ro.miui.region", CodeInjection.MD5))) {
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
        if (this.mShortcut != null) {
            int i = (this.mLastOrientation == 1 && !this.mSuperSave && (this.mDisable2 & 1) == 0) ? 0 : 8;
            this.mShortcut.setVisibility(i);
            updateShortCutVisibility(i);
        }
    }

    @Override // com.android.systemui.qs.MiuiHeaderView
    public void disable(int i) {
        super.disable(i);
        if (this.mDisable2 != i) {
            this.mDisable2 = i;
            updateShortCutVisibility();
        }
    }
}
