package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import com.miui.systemui.util.CommonUtil;

public class QSControlCenterHeaderView extends LinearLayout {
    /* access modifiers changed from: private */
    public ControlCenterActivityStarter mActStarter;
    private MiuiBatteryMeterView mBattery;
    /* access modifiers changed from: private */
    public MiuiClock mBigTime;
    private CarrierText mCarrierText;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public MiuiClock mDateView;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    private StatusBarIconController.MiuiLightDarkIconManager mIconManager;
    private Configuration mLastConfiguration;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        /* JADX WARNING: Removed duplicated region for block: B:13:0x0050  */
        /* JADX WARNING: Removed duplicated region for block: B:15:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClick(android.view.View r5) {
            /*
                r4 = this;
                com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView r0 = com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.this
                com.android.systemui.statusbar.policy.MiuiClock r0 = r0.mBigTime
                java.lang.String r1 = "android.intent.action.MAIN"
                r2 = 0
                if (r5 != r0) goto L_0x001b
                android.content.Intent r2 = new android.content.Intent
                r2.<init>(r1)
                java.lang.String r5 = "com.android.deskclock"
                r2.setPackage(r5)
                java.lang.String r5 = "clock"
            L_0x0017:
                r3 = r2
                r2 = r5
                r5 = r3
                goto L_0x004b
            L_0x001b:
                com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView r0 = com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.this
                com.android.systemui.statusbar.policy.MiuiClock r0 = r0.mDateView
                if (r5 != r0) goto L_0x0038
                android.content.Intent r2 = new android.content.Intent
                r2.<init>(r1)
                com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView r5 = com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.this
                android.content.Context r5 = r5.mContext
                java.lang.String r5 = com.android.systemui.controlcenter.utils.ControlCenterUtils.getCalendarPkg(r5)
                r2.setPackage(r5)
                java.lang.String r5 = "date"
                goto L_0x0017
            L_0x0038:
                com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView r0 = com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.this
                android.widget.ImageView r0 = r0.mShortcut
                if (r5 != r0) goto L_0x004a
                android.content.Intent r2 = new android.content.Intent
                java.lang.String r5 = "android.settings.SETTINGS"
                r2.<init>(r5)
                java.lang.String r5 = "settings"
                goto L_0x0017
            L_0x004a:
                r5 = r2
            L_0x004b:
                android.text.TextUtils.isEmpty(r2)
                if (r5 == 0) goto L_0x005e
                r0 = 268435456(0x10000000, float:2.5243549E-29)
                r5.addFlags(r0)
                com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView r4 = com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.this
                com.android.systemui.controlcenter.policy.ControlCenterActivityStarter r4 = r4.mActStarter
                r4.startActivity(r5)
            L_0x005e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.AnonymousClass1.onClick(android.view.View):void");
        }
    };
    private int mOrientation;
    private ControlPanelController mPanelController;
    /* access modifiers changed from: private */
    public ImageView mShortcut;
    private LinearLayout mStatusIcons;
    private ImageView mTilesEdit;
    private ViewGroup mTilesHeader;

    public QSControlCenterHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mLastConfiguration = new Configuration(context.getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        NetworkSpeedView networkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mFullscreenNetworkSpeedView = networkSpeedView;
        networkSpeedView.setVisibilityByStatusBar(true);
        this.mBattery = (MiuiBatteryMeterView) findViewById(C0015R$id.battery);
        this.mTilesHeader = (ViewGroup) findViewById(C0015R$id.tiles_header);
        this.mCarrierText = (CarrierText) findViewById(C0015R$id.carrier_text);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0015R$id.statusIcons);
        this.mStatusIcons = linearLayout;
        this.mIconManager = new StatusBarIconController.MiuiLightDarkIconManager(linearLayout, (CommandQueue) Dependency.get(CommandQueue.class), true, ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone());
        this.mOrientation = getOrientation();
        this.mDateView = (MiuiClock) findViewById(C0015R$id.date_time);
        this.mBigTime = (MiuiClock) findViewById(C0015R$id.big_time);
        this.mDateView.setOnClickListener(this.mOnClickListener);
        this.mBigTime.setOnClickListener(this.mOnClickListener);
        ImageView imageView = (ImageView) findViewById(C0015R$id.tiles_edit);
        this.mTilesEdit = imageView;
        imageView.setContentDescription(this.mContext.getResources().getString(C0021R$string.accessibility_desc_quick_settings_edit));
        ControlCenterUtils.createIconFolmeTouchStyle(this.mTilesEdit);
        int i = 0;
        this.mTilesEdit.setVisibility((this.mOrientation != 1 || this.mPanelController.isSuperPowerMode()) ? 8 : 0);
        ImageView imageView2 = (ImageView) findViewById(C0015R$id.control_center_shortcut);
        this.mShortcut = imageView2;
        ControlCenterUtils.createIconFolmeTouchStyle(imageView2);
        this.mShortcut.setImageResource(C0013R$drawable.qs_control_settings);
        this.mShortcut.setContentDescription(getResources().getString(C0021R$string.accessibility_settings));
        this.mShortcut.setOnClickListener(this.mOnClickListener);
        ImageView imageView3 = this.mShortcut;
        if (this.mPanelController.isSuperPowerMode()) {
            i = 8;
        }
        imageView3.setVisibility(i);
        this.mActStarter = (ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class);
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        updateLayout();
        updateHeaderColor();
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

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTilesEdit.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            updateLayout();
        }
        if (CommonUtil.isThemeResourcesChanged(this.mLastConfiguration.updateFrom(configuration), configuration.extraConfig.themeChangedFlags)) {
            updateHeaderColor();
        }
    }

    /* access modifiers changed from: protected */
    public void updateHeaderColor() {
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
        MiuiClock miuiClock2 = this.mBigTime;
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

    private void updateLayout() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mShortcut.getLayoutParams();
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mTilesHeader.getLayoutParams();
        int i = 8;
        if (this.mOrientation != 1 || this.mPanelController.isSuperPowerMode()) {
            layoutParams2.topMargin = 0;
            this.mTilesEdit.setVisibility(8);
            layoutParams.setMarginEnd(0);
        } else {
            layoutParams2.topMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_header_tiles_margin_top);
            this.mTilesEdit.setVisibility(0);
            layoutParams.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_button_margin_end));
        }
        this.mTilesHeader.setLayoutParams(layoutParams2);
        this.mShortcut.setLayoutParams(layoutParams);
        ViewGroup viewGroup = this.mTilesHeader;
        if (this.mOrientation == 1) {
            i = 0;
        }
        viewGroup.setVisibility(i);
    }

    public void updateResources() {
        this.mTilesEdit.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_control_tiles_edit));
        this.mShortcut.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_control_settings));
        this.mBigTime.setTextColor(this.mContext.getColor(C0011R$color.qs_control_header_clock_color));
        this.mDateView.setTextColor(this.mContext.getColor(C0011R$color.qs_control_header_date_color));
    }
}
