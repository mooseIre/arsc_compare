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
    private ControlCenterActivityStarter mActStarter;
    private MiuiBatteryMeterView mBattery;
    private MiuiClock mBigTime;
    private CarrierText mCarrierText;
    private Context mContext;
    private MiuiClock mDateView;
    private NetworkSpeedView mFullscreenNetworkSpeedView;
    private StatusBarIconController.MiuiLightDarkIconManager mIconManager;
    private Configuration mLastConfiguration;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        /* class com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.AnonymousClass1 */

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0053  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0060  */
        /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClick(android.view.View r6) {
            /*
            // Method dump skipped, instructions count: 111
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView.AnonymousClass1.onClick(android.view.View):void");
        }
    };
    private int mOrientation;
    private ControlPanelController mPanelController;
    private ViewGroup mPanelHeader;
    private ImageView mShortcut;
    private LinearLayout mStatusIcons;
    private ImageView mTilesEdit;
    private ViewGroup mTilesHeader;

    public QSControlCenterHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mLastConfiguration = new Configuration(context.getResources().getConfiguration());
    }

    public ViewGroup getTilesHeader() {
        return this.mTilesHeader;
    }

    public ViewGroup getPanelHeader() {
        return this.mPanelHeader;
    }

    public View getShortCut() {
        return this.mShortcut;
    }

    public View getDateTime() {
        return this.mDateView;
    }

    public View getBigTime() {
        return this.mBigTime;
    }

    public View getEditTile() {
        return this.mTilesEdit;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        NetworkSpeedView networkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mFullscreenNetworkSpeedView = networkSpeedView;
        networkSpeedView.setVisibilityByStatusBar(true);
        this.mBattery = (MiuiBatteryMeterView) findViewById(C0015R$id.battery);
        this.mTilesHeader = (ViewGroup) findViewById(C0015R$id.tiles_header);
        this.mPanelHeader = (ViewGroup) findViewById(C0015R$id.panel_header);
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
