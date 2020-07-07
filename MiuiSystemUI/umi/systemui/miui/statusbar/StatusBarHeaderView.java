package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Constants;
import com.android.systemui.CustomizedUtils;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.HeaderView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.tuner.TunerService;
import java.io.File;

public class StatusBarHeaderView extends HeaderView {
    private static int SDK_INT = Build.VERSION.SDK_INT;
    private LinearLayout mCarrierLayout;
    private CarrierText mCarrierText;
    private CarrierText mCarrierTextLand;
    private Typeface mClockTypeface;
    private int mDarkModeIconColorSingleTone;
    private boolean mHasMobileDataFeature;
    private StatusBarIconController.DarkIconManager mIconManager;
    private boolean mLayoutChangedForCarrierInLand;
    private boolean mLayoutChangedForCarrierInPortrait;
    private int mLightModeIconColorSingleTone;
    private LinearLayout mStatusIcons;
    private LinearLayout mSystemIcons;
    private LinearLayout mSystemIconsArea;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void onTuningChanged(String str, String str2) {
    }

    public StatusBarHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHasMobileDataFeature = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDateView.setClockMode(1);
        if (new File("system/fonts/MitypeVF.ttf").exists()) {
            this.mClockTypeface = Typeface.create("mitype-regular", 0);
        } else {
            this.mClockTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Mitype2018-50.otf");
        }
        this.mClock.setTypeface(this.mClockTypeface);
        this.mClock.setShowAmPm(false);
        this.mSystemIcons = (LinearLayout) findViewById(R.id.system_icons);
        this.mSystemIconsArea = (LinearLayout) findViewById(R.id.system_icon_area);
        ((BatteryMeterView) this.mSystemIcons.findViewById(R.id.battery)).setInHeader(true);
        this.mCarrierText = (CarrierText) findViewById(R.id.carrier);
        this.mCarrierTextLand = (CarrierText) findViewById(R.id.carrier_land);
        this.mCarrierLayout = (LinearLayout) findViewById(R.id.carrier_layout);
        this.mCarrierText.setShowStyle(-1);
        this.mCarrierTextLand.setShowStyle(-1);
        if (!this.mHasMobileDataFeature) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
            layoutParams.removeRule(6);
            this.mDateView.setLayoutParams(layoutParams);
            LinearLayout linearLayout = (LinearLayout) this.mSystemIconsArea.findViewById(R.id.signal_cluster_view);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
            layoutParams2.height = -1;
            linearLayout.setLayoutParams(layoutParams2);
        }
        this.mDarkModeIconColorSingleTone = this.mContext.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        this.mStatusIcons = (LinearLayout) findViewById(R.id.statusIcons);
        CustomizedUtils.checkRegion();
        updateCarrierText(getResources().getConfiguration().orientation);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateResources(getResources().getConfiguration());
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "status_bar_notification_shade_shortcut");
        this.mIconManager = new StatusBarIconController.DarkIconManager(this.mStatusIcons, true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        this.mIconManager.destroy();
        this.mIconManager = null;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources(configuration);
    }

    private void updateResources(Configuration configuration) {
        int i = configuration.orientation;
        if (i != this.mLastOrientation) {
            this.mClock.setTextAppearance(this.mContext, R.style.TextAppearance_StatusBar_Expanded_Clock_Notch);
            this.mClock.setTypeface(this.mClockTypeface);
        }
        if (i != this.mLastOrientation && !Constants.IS_TABLET) {
            int i2 = 1;
            int i3 = i == 1 ? 0 : 8;
            this.mClock.setVisibility(i3);
            this.mShortcut.setVisibility(i3);
            updateCarrierText(i);
            Clock clock = this.mDateView;
            if (i != 1) {
                i2 = 2;
            }
            clock.setClockMode(i2);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            marginLayoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.expanded_notification_header_bottom);
            setLayoutParams(marginLayoutParams);
            this.mLastOrientation = i;
        }
    }

    private void updateCarrierText(int i) {
        if (CustomizedUtils.isCarrierInHeaderViewShown()) {
            if (i == 1) {
                this.mLayoutChangedForCarrierInPortrait = true;
                this.mCarrierText.setShowStyle(1);
                this.mCarrierTextLand.setShowStyle(-1);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
                layoutParams.removeRule(12);
                layoutParams.removeRule(6);
                layoutParams.addRule(8, R.id.notification_shade_shortcut);
                this.mDateView.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mCarrierLayout.getLayoutParams();
                layoutParams2.addRule(6, R.id.system_icon_area);
                this.mCarrierLayout.setLayoutParams(layoutParams2);
                RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mSystemIconsArea.getLayoutParams();
                layoutParams3.addRule(17, R.id.carrier_layout);
                this.mSystemIconsArea.setLayoutParams(layoutParams3);
                return;
            }
            this.mLayoutChangedForCarrierInLand = true;
            this.mCarrierText.setShowStyle(-1);
            this.mCarrierTextLand.setShowStyle(1);
            RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mCarrierLayout.getLayoutParams();
            layoutParams4.removeRule(6);
            this.mCarrierLayout.setLayoutParams(layoutParams4);
            RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
            layoutParams5.removeRule(8);
            layoutParams5.addRule(12);
            layoutParams5.addRule(6, R.id.system_icon_area);
            this.mDateView.setLayoutParams(layoutParams5);
            RelativeLayout.LayoutParams layoutParams6 = (RelativeLayout.LayoutParams) this.mSystemIconsArea.getLayoutParams();
            layoutParams6.addRule(17, R.id.carrier_land_layout);
            this.mSystemIconsArea.setLayoutParams(layoutParams6);
        } else if (SDK_INT >= 24) {
        } else {
            if (this.mLayoutChangedForCarrierInPortrait || this.mLayoutChangedForCarrierInLand) {
                this.mCarrierText.setShowStyle(-1);
                this.mCarrierTextLand.setShowStyle(-1);
                if (this.mLayoutChangedForCarrierInPortrait && i == 1) {
                    this.mLayoutChangedForCarrierInPortrait = false;
                    RelativeLayout.LayoutParams layoutParams7 = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
                    layoutParams7.addRule(12);
                    layoutParams7.addRule(6, R.id.system_icon_area);
                    layoutParams7.removeRule(8);
                    this.mDateView.setLayoutParams(layoutParams7);
                    RelativeLayout.LayoutParams layoutParams8 = (RelativeLayout.LayoutParams) this.mCarrierLayout.getLayoutParams();
                    layoutParams8.removeRule(6);
                    this.mCarrierLayout.setLayoutParams(layoutParams8);
                    RelativeLayout.LayoutParams layoutParams9 = (RelativeLayout.LayoutParams) this.mSystemIconsArea.getLayoutParams();
                    layoutParams9.removeRule(17);
                    this.mSystemIconsArea.setLayoutParams(layoutParams9);
                } else if (this.mLayoutChangedForCarrierInLand && i == 2) {
                    this.mLayoutChangedForCarrierInLand = false;
                    RelativeLayout.LayoutParams layoutParams10 = (RelativeLayout.LayoutParams) this.mCarrierLayout.getLayoutParams();
                    layoutParams10.removeRule(6);
                    this.mCarrierLayout.setLayoutParams(layoutParams10);
                    RelativeLayout.LayoutParams layoutParams11 = (RelativeLayout.LayoutParams) this.mDateView.getLayoutParams();
                    layoutParams11.removeRule(12);
                    layoutParams11.removeRule(6);
                    layoutParams11.addRule(8);
                    this.mDateView.setLayoutParams(layoutParams11);
                    RelativeLayout.LayoutParams layoutParams12 = (RelativeLayout.LayoutParams) this.mSystemIconsArea.getLayoutParams();
                    layoutParams12.removeRule(17);
                    this.mSystemIconsArea.setLayoutParams(layoutParams12);
                }
            }
        }
    }

    public void themeChanged() {
        boolean z = getContext().getResources().getBoolean(R.bool.expanded_status_bar_darkmode);
        float f = z ? 1.0f : 0.0f;
        Rect rect = new Rect(0, 0, 0, 0);
        int i = z ? this.mDarkModeIconColorSingleTone : this.mLightModeIconColorSingleTone;
        for (int i2 = 0; i2 < this.mSystemIcons.getChildCount(); i2++) {
            View childAt = this.mSystemIcons.getChildAt(i2);
            if (childAt instanceof DarkIconDispatcher.DarkReceiver) {
                ((DarkIconDispatcher.DarkReceiver) childAt).onDarkChanged(rect, f, i);
            }
        }
        StatusBarIconController.DarkIconManager darkIconManager = this.mIconManager;
        if (darkIconManager != null) {
            darkIconManager.setDarkIntensity(rect, f, i);
        }
    }

    public void regionChanged() {
        CustomizedUtils.checkRegion();
        updateCarrierText(this.mLastOrientation);
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
}
