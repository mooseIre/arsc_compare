package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.AnimatedImageView;
import com.android.systemui.statusbar.NetworkSpeedSplitter;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.RegionController;
import com.android.systemui.statusbar.phone.CollapsedStatusBarFragment;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.xiaomi.stat.MiStat;
import java.util.ArrayList;
import java.util.Arrays;

public class CollapsedStatusBarFragmentControllerDripImpl implements CollapsedStatusBarFragmentController, HotspotController.Callback, NetworkController.SignalCallback, RegionController.Callback {
    private CarrierText mCarrierText;
    private Clock mClock;
    /* access modifiers changed from: private */
    public ConfigurationController.ConfigurationListener mConfigurationListener;
    /* access modifiers changed from: private */
    public View mDripLeftEarSuperContainer;
    private NetworkSpeedView mDripNetwokSpeedView;
    private AnimatedImageView mDripWifiApOn;
    /* access modifiers changed from: private */
    public int mExtraSpace;
    /* access modifiers changed from: private */
    public CollapsedStatusBarFragment mFragment;
    private HotspotController mHotspot;
    private NetworkController mNetworkController;
    private StatusBarIconController.OrderedIconManager mOrderedIconManager;
    private StatusBarIconController.OrderedIconManager mOrderedRightIconManager;
    /* access modifiers changed from: private */
    public ViewGroup mPhoneStatusBarContainer;
    private boolean mShowCarrierText;
    private boolean mShowCarrierTextForRegion;
    private AnimatedImageView mSlaveWifi;
    private NetworkSpeedSplitter mSplitter;
    /* access modifiers changed from: private */
    public View mSystemIconArea;

    public int getLayoutId() {
        return R.layout.drip_status_bar_contents_container;
    }

    public boolean isClockVisibleByPrompt(boolean z) {
        return true;
    }

    public boolean isGPSDriveModeVisible() {
        return false;
    }

    public boolean isNarrowNotch() {
        return false;
    }

    public boolean isNotch() {
        return true;
    }

    public boolean isStatusIconsVisible() {
        return false;
    }

    public void init(CollapsedStatusBarFragment collapsedStatusBarFragment) {
        this.mHotspot = (HotspotController) Dependency.get(HotspotController.class);
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mFragment = collapsedStatusBarFragment;
    }

    public void initViews(View view) {
        this.mDripLeftEarSuperContainer = this.mFragment.mClockContainer.findViewById(R.id.drip_leftear_super_container);
        this.mDripNetwokSpeedView = (NetworkSpeedView) this.mFragment.mClockContainer.findViewById(R.id.drip_network_speed_view);
        this.mClock = (Clock) view.findViewById(R.id.clock);
        this.mSplitter = (NetworkSpeedSplitter) view.findViewById(R.id.network_speed_splitter);
        this.mSystemIconArea = view.findViewById(R.id.system_icon_area);
        this.mCarrierText = (CarrierText) view.findViewById(R.id.carrier);
        this.mShowCarrierText = view.getContext().getResources().getBoolean(R.bool.status_bar_show_carrier);
        updateCarrierStyle();
        this.mDripWifiApOn = (AnimatedImageView) view.findViewById(R.id.drip_wifi_ap_on);
        this.mSlaveWifi = (AnimatedImageView) view.findViewById(R.id.drip_slave_wifi);
        AnonymousClass1 r0 = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerDripImpl.this.mFragment.mClockContainer, true, false, 0);
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerDripImpl.this.mSystemIconArea, false, false, CollapsedStatusBarFragmentControllerDripImpl.this.mExtraSpace);
            }
        };
        this.mConfigurationListener = r0;
        r0.onDensityOrFontScaleChanged();
        this.mPhoneStatusBarContainer = (ViewGroup) view.findViewById(R.id.phone_status_bar_contents_container);
        ((BatteryMeterView) this.mSystemIconArea.findViewById(R.id.battery)).setBatteryMeterViewDelegate(new BatteryMeterView.BatteryMeterViewDelegate() {
            public void onNumberToIconChanged(boolean z) {
                CollapsedStatusBarFragmentControllerDripImpl collapsedStatusBarFragmentControllerDripImpl = CollapsedStatusBarFragmentControllerDripImpl.this;
                int unused = collapsedStatusBarFragmentControllerDripImpl.mExtraSpace = z ? collapsedStatusBarFragmentControllerDripImpl.mPhoneStatusBarContainer.getContext().getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width) : 0;
                DripStatusBarUtils.updateContainerEndMargin(CollapsedStatusBarFragmentControllerDripImpl.this.mPhoneStatusBarContainer, CollapsedStatusBarFragmentControllerDripImpl.this.mExtraSpace);
                CollapsedStatusBarFragmentControllerDripImpl.this.mConfigurationListener.onDensityOrFontScaleChanged();
            }
        });
    }

    public void start(View view) {
        NetworkSpeedSplitter networkSpeedSplitter;
        Class cls = StatusBarIconController.class;
        Class cls2 = DarkIconDispatcher.class;
        Clock clock = this.mClock;
        if (!(clock == null || this.mDripNetwokSpeedView == null || (networkSpeedSplitter = this.mSplitter) == null)) {
            boolean z = true;
            networkSpeedSplitter.onClockVisibilityChanged(clock.getVisibility() == 0);
            NetworkSpeedSplitter networkSpeedSplitter2 = this.mSplitter;
            if (this.mDripNetwokSpeedView.getVisibility() != 0) {
                z = false;
            }
            networkSpeedSplitter2.onNetworkSpeedVisibilityChanged(z);
            this.mClock.addVisibilityListener(this.mSplitter);
            this.mDripNetwokSpeedView.addVisibilityListener(this.mSplitter);
        }
        ((DarkIconDispatcher) Dependency.get(cls2)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        ((DarkIconDispatcher) Dependency.get(cls2)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetwokSpeedView);
        ((DarkIconDispatcher) Dependency.get(cls2)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mSplitter);
        this.mOrderedIconManager = new StatusBarIconController.OrderedIconManager(this.mFragment.mNotchLeftEarIcons, new ArrayList(Arrays.asList(new String[]{"quiet", "volume", "alarm_clock", "headset", "micphone", "ble_unlock_mode"})));
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mOrderedIconManager);
        ((DarkIconDispatcher) Dependency.get(cls2)).addDarkReceiver((ImageView) this.mDripWifiApOn);
        this.mHotspot.addCallback(this);
        this.mOrderedRightIconManager = new StatusBarIconController.OrderedIconManager(this.mFragment.mStatusIcons, new ArrayList(Arrays.asList(new String[]{"managed_profile", MiStat.Param.LOCATION, "bluetooth"})));
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mOrderedRightIconManager);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
        this.mNetworkController.addCallback(this);
        ((DarkIconDispatcher) Dependency.get(cls2)).addDarkReceiver((ImageView) this.mSlaveWifi);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    public void stop() {
        NetworkSpeedSplitter networkSpeedSplitter;
        Class cls = StatusBarIconController.class;
        Class cls2 = DarkIconDispatcher.class;
        if (this.mOrderedIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mOrderedIconManager);
        }
        if (this.mOrderedRightIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mOrderedRightIconManager);
        }
        if (this.mDripNetwokSpeedView != null) {
            ((DarkIconDispatcher) Dependency.get(cls2)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetwokSpeedView);
        }
        if (this.mSplitter != null) {
            ((DarkIconDispatcher) Dependency.get(cls2)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mSplitter);
        }
        if (this.mDripWifiApOn != null) {
            ((DarkIconDispatcher) Dependency.get(cls2)).removeDarkReceiver((ImageView) this.mDripWifiApOn);
        }
        if (this.mCarrierText != null) {
            ((DarkIconDispatcher) Dependency.get(cls2)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        }
        HotspotController hotspotController = this.mHotspot;
        if (hotspotController != null) {
            hotspotController.removeCallback(this);
        }
        if (this.mConfigurationListener != null) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        }
        Clock clock = this.mClock;
        if (!(clock == null || this.mDripNetwokSpeedView == null || (networkSpeedSplitter = this.mSplitter) == null)) {
            clock.removeVisibilityListener(networkSpeedSplitter);
            this.mDripNetwokSpeedView.removeVisibilityListener(this.mSplitter);
        }
        if (this.mSlaveWifi != null) {
            ((DarkIconDispatcher) Dependency.get(cls2)).removeDarkReceiver((ImageView) this.mSlaveWifi);
        }
        NetworkController networkController = this.mNetworkController;
        if (networkController != null) {
            networkController.removeCallback(this);
        }
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
    }

    public void hideSystemIconArea(boolean z, boolean z2) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null) {
            View view = this.mDripLeftEarSuperContainer;
            if (view != null) {
                collapsedStatusBarFragment.animateHide(view, z, z2);
            }
            CarrierText carrierText = this.mCarrierText;
            if (carrierText != null) {
                carrierText.forceHide(true);
            }
        }
    }

    public void showSystemIconArea(boolean z) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null) {
            View view = this.mDripLeftEarSuperContainer;
            if (view != null) {
                collapsedStatusBarFragment.animateShow(view, z);
            }
            CarrierText carrierText = this.mCarrierText;
            if (carrierText != null) {
                carrierText.forceHide(false);
            }
        }
    }

    public void updateLeftPartVisibility(final boolean z, boolean z2, boolean z3, boolean z4) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null && this.mDripLeftEarSuperContainer != null && z3) {
            if (!z2) {
                collapsedStatusBarFragment.clockVisibleAnimate(z, true);
                return;
            }
            if (!z) {
                collapsedStatusBarFragment.clockVisibleAnimate(false, true);
            }
            this.mFragment.animateHideWithCallback(this.mDripLeftEarSuperContainer, true, false, new CollapsedStatusBarFragment.HideAnimateCallback() {
                public void callOnEnd() {
                    if (z) {
                        CollapsedStatusBarFragmentControllerDripImpl.this.mFragment.clockVisibleAnimate(true, true);
                    }
                    CollapsedStatusBarFragmentControllerDripImpl.this.mFragment.animateShow(CollapsedStatusBarFragmentControllerDripImpl.this.mDripLeftEarSuperContainer, true);
                }
            });
        }
    }

    public void onHotspotChanged(boolean z) {
        AnimatedImageView animatedImageView = this.mDripWifiApOn;
        if (animatedImageView != null) {
            animatedImageView.setVisibility(z ? 0 : 8);
        }
    }

    public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        AnimatedImageView animatedImageView = this.mSlaveWifi;
        if (animatedImageView != null) {
            if (iconState.visible) {
                animatedImageView.setImageResource(iconState.icon);
                this.mSlaveWifi.setVisibility(0);
                return;
            }
            animatedImageView.setVisibility(8);
        }
    }

    public void onRegionChanged(String str) {
        this.mShowCarrierTextForRegion = str.equals("SA");
        updateCarrierStyle();
    }

    private void updateCarrierStyle() {
        if (this.mShowCarrierText || this.mShowCarrierTextForRegion) {
            this.mCarrierText.setShowStyle(1);
        } else {
            this.mCarrierText.setShowStyle(-1);
        }
    }
}
