package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.AnimatedImageView;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.xiaomi.stat.MiStat;
import java.util.ArrayList;
import java.util.Arrays;

public class KeyguardStatusBarViewControllerDripImpl implements KeyguardStatusBarViewController, HotspotController.Callback, MiuiStatusBarPromptController.OnPromptStateChangedListener, NetworkController.SignalCallback {
    private static final String TAG = "KeyguardStatusBarViewControllerDripImpl";
    /* access modifiers changed from: private */
    public View mCarrierSuperContainer;
    /* access modifiers changed from: private */
    public ConfigurationController.ConfigurationListener mConfigurationListener;
    private float mDarkIntensity;
    /* access modifiers changed from: private */
    public int mExtraSpace;
    private ClipEdgeLinearLayout mKeyguardDripLeftearIcons;
    private AnimatedImageView mKeyguardDripWifiApOn;
    private AnimatedImageView mKeyguardSlaveWifi;
    private StatusBarIconController.OrderedIconManager mOrderedIconManager;
    /* access modifiers changed from: private */
    public ViewGroup mPaddingContaienr;
    private StatusBarIconController.OrderedIconManager mRightOrderedIconManager;
    private int mSlaveWifiIcon;
    private KeyguardStatusBarView mStatusBarView;
    private Rect mTintRect;

    public int getLayoutId() {
        return R.layout.keyguard_drip_status_bar_system_icons_container;
    }

    public boolean isNotch() {
        return true;
    }

    public boolean isPromptCenter() {
        return false;
    }

    public void init(KeyguardStatusBarView keyguardStatusBarView) {
        this.mStatusBarView = keyguardStatusBarView;
        this.mKeyguardDripLeftearIcons = (ClipEdgeLinearLayout) this.mStatusBarView.findViewById(R.id.keyguard_leftear_icons);
        this.mCarrierSuperContainer = this.mStatusBarView.findViewById(R.id.keyguard_carrier_super_container);
        this.mPaddingContaienr = (ViewGroup) this.mStatusBarView.findViewById(R.id.system_icons_padding_container);
        this.mConfigurationListener = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                DripStatusBarUtils.updateContainerWidth(KeyguardStatusBarViewControllerDripImpl.this.mCarrierSuperContainer, true, true, 0);
                DripStatusBarUtils.updateContainerWidth(KeyguardStatusBarViewControllerDripImpl.this.mPaddingContaienr, false, true, KeyguardStatusBarViewControllerDripImpl.this.mExtraSpace);
            }
        };
        this.mConfigurationListener.onDensityOrFontScaleChanged();
        ((BatteryMeterView) this.mStatusBarView.findViewById(R.id.battery)).setBatteryMeterViewDelegate(new BatteryMeterView.BatteryMeterViewDelegate() {
            public void onNumberToIconChanged(boolean z) {
                KeyguardStatusBarViewControllerDripImpl keyguardStatusBarViewControllerDripImpl = KeyguardStatusBarViewControllerDripImpl.this;
                int unused = keyguardStatusBarViewControllerDripImpl.mExtraSpace = z ? keyguardStatusBarViewControllerDripImpl.mPaddingContaienr.getContext().getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width) : 0;
                DripStatusBarUtils.updateContainerEndMargin(KeyguardStatusBarViewControllerDripImpl.this.mPaddingContaienr, KeyguardStatusBarViewControllerDripImpl.this.mExtraSpace);
                KeyguardStatusBarViewControllerDripImpl.this.mConfigurationListener.onDensityOrFontScaleChanged();
            }
        });
        this.mTintRect = new Rect();
    }

    public void destroy() {
        this.mStatusBarView = null;
    }

    public void updateNotchVisible() {
        this.mKeyguardDripLeftearIcons.setVisibility(0);
    }

    public void showStatusIcons() {
        ((HotspotController) Dependency.get(HotspotController.class)).addCallback(this);
        this.mOrderedIconManager = new StatusBarIconController.OrderedIconManager(this.mKeyguardDripLeftearIcons, new ArrayList(Arrays.asList(new String[]{"quiet", "volume", "alarm_clock", "headset", "micphone", "ble_unlock_mode"})), true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mOrderedIconManager);
        this.mKeyguardDripWifiApOn = (AnimatedImageView) this.mStatusBarView.findViewById(R.id.drip_wifi_ap_on);
        this.mRightOrderedIconManager = new StatusBarIconController.OrderedIconManager(this.mStatusBarView.mStatusIcons, new ArrayList(Arrays.asList(new String[]{MiStat.Param.LOCATION, "bluetooth"})), true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mRightOrderedIconManager);
        this.mKeyguardSlaveWifi = (AnimatedImageView) this.mStatusBarView.findViewById(R.id.drip_slave_wifi);
        ((NetworkController) Dependency.get(NetworkController.class)).addCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addAndUpdatePromptStateChangedListener(TAG, this);
    }

    public void hideStatusIcons() {
        ((HotspotController) Dependency.get(HotspotController.class)).removeCallback(this);
        if (this.mOrderedIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mOrderedIconManager);
        }
        if (this.mRightOrderedIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mRightOrderedIconManager);
        }
        if (this.mConfigurationListener != null) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        }
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).removePromptStateChangedListener(TAG);
        ((NetworkController) Dependency.get(NetworkController.class)).removeCallback(this);
    }

    public void setDarkMode(Rect rect, float f, int i) {
        StatusBarIconController.OrderedIconManager orderedIconManager = this.mOrderedIconManager;
        if (orderedIconManager != null) {
            orderedIconManager.setDarkIntensity(rect, f, i);
        }
        StatusBarIconController.OrderedIconManager orderedIconManager2 = this.mRightOrderedIconManager;
        if (orderedIconManager2 != null) {
            orderedIconManager2.setDarkIntensity(rect, f, i);
        }
        this.mStatusBarView.setDarkMode(this.mKeyguardDripLeftearIcons, rect, f);
        AnimatedImageView animatedImageView = this.mKeyguardDripWifiApOn;
        if (animatedImageView != null) {
            animatedImageView.setImageResource(Icons.get(Integer.valueOf(R.drawable.stat_sys_wifi_ap_on), DarkIconDispatcherHelper.inDarkMode(this.mTintRect, this.mKeyguardDripWifiApOn, this.mDarkIntensity)));
        }
        AnimatedImageView animatedImageView2 = this.mKeyguardSlaveWifi;
        if (animatedImageView2 != null) {
            animatedImageView2.setImageResource(Icons.get(Integer.valueOf(this.mSlaveWifiIcon), DarkIconDispatcherHelper.inDarkMode(this.mTintRect, this.mKeyguardSlaveWifi, this.mDarkIntensity)));
        }
        this.mTintRect.set(rect);
        this.mDarkIntensity = f;
    }

    public void onHotspotChanged(boolean z) {
        AnimatedImageView animatedImageView = this.mKeyguardDripWifiApOn;
        if (animatedImageView != null) {
            animatedImageView.setVisibility(z ? 0 : 8);
        }
    }

    public void onPromptStateChanged(boolean z, String str) {
        this.mKeyguardDripLeftearIcons.setVisibility(0);
    }

    public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        AnimatedImageView animatedImageView = this.mKeyguardSlaveWifi;
        if (animatedImageView != null) {
            if (iconState.visible) {
                int i = iconState.icon;
                this.mSlaveWifiIcon = i;
                animatedImageView.setImageResource(Icons.get(Integer.valueOf(i), DarkIconDispatcherHelper.inDarkMode(this.mTintRect, this.mKeyguardSlaveWifi, this.mDarkIntensity)));
                this.mKeyguardSlaveWifi.setVisibility(0);
                return;
            }
            animatedImageView.setVisibility(8);
        }
    }
}
