package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.util.Utils;
import miui.view.MiuiHapticFeedbackConstants;

public class QSControlCenterHeaderView extends LinearLayout {
    /* access modifiers changed from: private */
    public ControlCenterActivityStarter mActStarter;
    /* access modifiers changed from: private */
    public Clock mBigTime;
    private LinearLayout mCarrierLayout;
    private CarrierText mCarrierText;
    private int mDarkModeIconColorSingleTone;
    /* access modifiers changed from: private */
    public Clock mDateView;
    private StatusBarIconController.DarkIconManager mIconManager;
    private int mLightModeIconColorSingleTone;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String str;
            Intent intent = null;
            if (view == QSControlCenterHeaderView.this.mBigTime) {
                intent = new Intent("android.intent.action.MAIN");
                intent.setPackage("com.android.deskclock");
                str = "clock";
            } else if (view == QSControlCenterHeaderView.this.mDateView) {
                intent = new Intent("android.intent.action.MAIN");
                intent.setPackage(Utils.getCalendarPkg(QSControlCenterHeaderView.this.mContext));
                str = "date";
            } else if (view == QSControlCenterHeaderView.this.mShortcut) {
                intent = new Intent("android.settings.SETTINGS");
                str = "settings";
            } else {
                str = null;
            }
            if (!TextUtils.isEmpty(str)) {
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("settings");
            }
            if (intent != null) {
                intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                QSControlCenterHeaderView.this.mActStarter.startActivity(intent);
            }
        }
    };
    private int mOrientation;
    private ControlPanelController mPanelController = ((ControlPanelController) Dependency.get(ControlPanelController.class));
    /* access modifiers changed from: private */
    public ImageView mShortcut;
    private LinearLayout mStatusIcons;
    private ImageView mTilesEdit;
    private ViewGroup mTilesHeader;

    public QSControlCenterHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        int i = 0;
        ((NetworkSpeedView) findViewById(R.id.network_speed_view)).setVisibility(0);
        ClipEdgeLinearLayout clipEdgeLinearLayout = (ClipEdgeLinearLayout) findViewById(R.id.system_icons);
        clipEdgeLinearLayout.setClipEdge(true);
        ((SignalClusterView) clipEdgeLinearLayout.findViewById(R.id.signal_cluster)).setForceNormalType();
        this.mTilesHeader = (ViewGroup) findViewById(R.id.tiles_header);
        this.mCarrierLayout = (LinearLayout) findViewById(R.id.carrier_layout);
        this.mCarrierText = (CarrierText) findViewById(R.id.carrier_text);
        this.mCarrierText.setShowStyle(1);
        this.mOrientation = getOrientation();
        this.mDarkModeIconColorSingleTone = this.mContext.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        this.mStatusIcons = (LinearLayout) findViewById(R.id.statusIcons);
        this.mDateView = (Clock) findViewById(R.id.date_time);
        this.mBigTime = (Clock) findViewById(R.id.big_time);
        this.mDateView.setClockMode(3);
        this.mDateView.setOnClickListener(this.mOnClickListener);
        this.mBigTime.setClockMode(0);
        this.mBigTime.setShowAmPm(false);
        this.mBigTime.setOnClickListener(this.mOnClickListener);
        this.mTilesEdit = (ImageView) findViewById(R.id.tiles_edit);
        this.mTilesEdit.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_desc_quick_settings_edit));
        Utils.createIconFolmeTouchStyle(this.mTilesEdit);
        this.mTilesEdit.setVisibility((this.mOrientation != 1 || this.mPanelController.isSuperPowerMode()) ? 8 : 0);
        this.mShortcut = (ImageView) findViewById(R.id.notification_shade_shortcut);
        Utils.createIconFolmeTouchStyle(this.mShortcut);
        this.mShortcut.setImageResource(R.drawable.qs_control_settings);
        this.mShortcut.setContentDescription(getResources().getString(R.string.accessibility_settings));
        this.mShortcut.setOnClickListener(this.mOnClickListener);
        ImageView imageView = this.mShortcut;
        if (this.mPanelController.isSuperPowerMode()) {
            i = 8;
        }
        imageView.setVisibility(i);
        this.mActStarter = (ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIconManager = new StatusBarIconController.DarkIconManager(this.mStatusIcons, true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
        this.mIconManager = null;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mShortcut.getLayoutParams();
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mTilesHeader.getLayoutParams();
            if (this.mOrientation != 1 || this.mPanelController.isSuperPowerMode()) {
                layoutParams2.topMargin = 0;
                this.mTilesEdit.setVisibility(8);
                layoutParams.setMarginEnd(0);
            } else {
                layoutParams2.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_header_tiles_margin_top);
                this.mTilesEdit.setVisibility(0);
                layoutParams.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_button_margin_end));
            }
            this.mTilesHeader.setLayoutParams(layoutParams2);
            this.mShortcut.setLayoutParams(layoutParams);
        }
    }

    public void updateResources() {
        this.mTilesEdit.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_tiles_edit));
        this.mShortcut.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_settings));
        this.mBigTime.setTextColor(this.mContext.getColor(R.color.qs_control_header_clock_color));
        this.mDateView.setTextColor(this.mContext.getColor(R.color.qs_control_header_date_color));
    }
}
