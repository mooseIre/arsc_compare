package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;

public class QSControlCenterHeaderView extends LinearLayout {
    /* access modifiers changed from: private */
    public ControlCenterActivityStarter mActStarter;
    /* access modifiers changed from: private */
    public Clock mBigTime;
    private LinearLayout mCarrierLayout;
    private CarrierText mCarrierText;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Clock mDateView;
    private StatusBarIconController.DarkIconManager mIconManager;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        /* JADX WARNING: Removed duplicated region for block: B:13:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0061  */
        /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onClick(android.view.View r6) {
            /*
                r5 = this;
                com.android.systemui.miui.controlcenter.QSControlCenterHeaderView r0 = com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.this
                com.android.systemui.statusbar.policy.Clock r0 = r0.mBigTime
                java.lang.String r1 = "settings"
                java.lang.String r2 = "android.intent.action.MAIN"
                r3 = 0
                if (r6 != r0) goto L_0x001e
                android.content.Intent r3 = new android.content.Intent
                r3.<init>(r2)
                java.lang.String r6 = "com.android.deskclock"
                r3.setPackage(r6)
                java.lang.String r6 = "clock"
            L_0x001a:
                r4 = r3
                r3 = r6
                r6 = r4
                goto L_0x004e
            L_0x001e:
                com.android.systemui.miui.controlcenter.QSControlCenterHeaderView r0 = com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.this
                com.android.systemui.statusbar.policy.Clock r0 = r0.mDateView
                if (r6 != r0) goto L_0x003b
                android.content.Intent r3 = new android.content.Intent
                r3.<init>(r2)
                com.android.systemui.miui.controlcenter.QSControlCenterHeaderView r6 = com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.this
                android.content.Context r6 = r6.mContext
                java.lang.String r6 = com.android.systemui.util.Utils.getCalendarPkg(r6)
                r3.setPackage(r6)
                java.lang.String r6 = "date"
                goto L_0x001a
            L_0x003b:
                com.android.systemui.miui.controlcenter.QSControlCenterHeaderView r0 = com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.this
                android.widget.ImageView r0 = r0.mShortcut
                if (r6 != r0) goto L_0x004d
                android.content.Intent r3 = new android.content.Intent
                java.lang.String r6 = "android.settings.SETTINGS"
                r3.<init>(r6)
                r6 = r3
                r3 = r1
                goto L_0x004e
            L_0x004d:
                r6 = r3
            L_0x004e:
                boolean r0 = android.text.TextUtils.isEmpty(r3)
                if (r0 != 0) goto L_0x005f
                java.lang.Class<com.android.systemui.miui.statusbar.analytics.SystemUIStat> r0 = com.android.systemui.miui.statusbar.analytics.SystemUIStat.class
                java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
                com.android.systemui.miui.statusbar.analytics.SystemUIStat r0 = (com.android.systemui.miui.statusbar.analytics.SystemUIStat) r0
                r0.handleClickShortcutEvent(r1)
            L_0x005f:
                if (r6 == 0) goto L_0x006f
                r0 = 268435456(0x10000000, float:2.5243549E-29)
                r6.addFlags(r0)
                com.android.systemui.miui.controlcenter.QSControlCenterHeaderView r5 = com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.this
                com.android.systemui.miui.statusbar.ControlCenterActivityStarter r5 = r5.mActStarter
                r5.startActivity(r6)
            L_0x006f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.QSControlCenterHeaderView.AnonymousClass1.onClick(android.view.View):void");
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
        CarrierText carrierText = (CarrierText) findViewById(R.id.carrier_text);
        this.mCarrierText = carrierText;
        carrierText.setShowStyle(1);
        this.mOrientation = getOrientation();
        this.mContext.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        this.mStatusIcons = (LinearLayout) findViewById(R.id.statusIcons);
        this.mDateView = (Clock) findViewById(R.id.date_time);
        this.mBigTime = (Clock) findViewById(R.id.big_time);
        this.mDateView.setClockMode(3);
        this.mDateView.setOnClickListener(this.mOnClickListener);
        this.mBigTime.setClockMode(0);
        this.mBigTime.setShowAmPm(false);
        this.mBigTime.setOnClickListener(this.mOnClickListener);
        ImageView imageView = (ImageView) findViewById(R.id.tiles_edit);
        this.mTilesEdit = imageView;
        imageView.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_desc_quick_settings_edit));
        Utils.createIconFolmeTouchStyle(this.mTilesEdit);
        this.mTilesEdit.setVisibility((this.mOrientation != 1 || this.mPanelController.isSuperPowerMode()) ? 8 : 0);
        ImageView imageView2 = (ImageView) findViewById(R.id.notification_shade_shortcut);
        this.mShortcut = imageView2;
        Utils.createIconFolmeTouchStyle(imageView2);
        this.mShortcut.setImageResource(R.drawable.qs_control_settings);
        this.mShortcut.setContentDescription(getResources().getString(R.string.accessibility_settings));
        this.mShortcut.setOnClickListener(this.mOnClickListener);
        ImageView imageView3 = this.mShortcut;
        if (this.mPanelController.isSuperPowerMode()) {
            i = 8;
        }
        imageView3.setVisibility(i);
        this.mActStarter = (ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class);
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        updateLayout();
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
        this.mTilesEdit.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            updateLayout();
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
            layoutParams2.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_header_tiles_margin_top);
            this.mTilesEdit.setVisibility(0);
            layoutParams.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_button_margin_end));
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
        this.mTilesEdit.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_tiles_edit));
        this.mShortcut.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_settings));
        this.mBigTime.setTextColor(this.mContext.getColor(R.color.qs_control_header_clock_color));
        this.mDateView.setTextColor(this.mContext.getColor(R.color.qs_control_header_date_color));
    }
}
