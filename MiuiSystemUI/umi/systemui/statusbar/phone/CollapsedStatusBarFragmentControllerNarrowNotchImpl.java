package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.RegionController;
import com.android.systemui.statusbar.phone.CollapsedStatusBarFragment;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.xiaomi.stat.MiStat;
import java.util.Objects;

public class CollapsedStatusBarFragmentControllerNarrowNotchImpl extends CollapsedStatusBarFragmentControllerImpl {
    /* access modifiers changed from: private */
    public ConfigurationController.ConfigurationListener mConfigurationListener;
    /* access modifiers changed from: private */
    public int mExtraSpace;
    /* access modifiers changed from: private */
    public ViewGroup mPhoneStatusBarContainer;
    /* access modifiers changed from: private */
    public View mSystemIconArea;

    public boolean isClockVisibleByPrompt(boolean z) {
        return true;
    }

    public boolean isNarrowNotch() {
        return true;
    }

    public boolean isNotch() {
        return true;
    }

    public boolean isStatusIconsVisible() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWrapContent() {
        return false;
    }

    public void start(View view) {
        Class cls = StatusBarIconController.class;
        ArraySet<String> arraySet = this.mFragment.mNotchleftearIconsList;
        arraySet.add("bluetooth");
        arraySet.add(MiStat.Param.LOCATION);
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        Objects.requireNonNull(collapsedStatusBarFragment);
        CollapsedStatusBarFragment.LeftEarIconManager leftEarIconManager = new CollapsedStatusBarFragment.LeftEarIconManager(this.mFragment.mNotchLeftEarIcons);
        this.mNotchLeftEarIconManager = leftEarIconManager;
        leftEarIconManager.mWhiteList = new ArraySet<>();
        this.mNotchLeftEarIconManager.mWhiteList.addAll(arraySet);
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager(this.mFragment.mStatusIcons);
        this.mDarkIconManager = darkIconManager;
        darkIconManager.mWhiteList = new ArraySet<>();
        this.mDarkIconManager.mWhiteList.add("volume");
        this.mDarkIconManager.mWhiteList.add("quiet");
        this.mDarkIconManager.mWhiteList.add("alarm_clock");
        this.mSystemIconArea = view.findViewById(R.id.system_icons);
        AnonymousClass1 r1 = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mFragment.mClockContainer, true, false, 0);
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mSystemIconArea, false, false, CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mExtraSpace);
            }
        };
        this.mConfigurationListener = r1;
        r1.onDensityOrFontScaleChanged();
        this.mPhoneStatusBarContainer = (ViewGroup) view.findViewById(R.id.phone_status_bar_contents_container);
        ((BatteryMeterView) this.mSystemIconArea.findViewById(R.id.battery)).setBatteryMeterViewDelegate(new BatteryMeterView.BatteryMeterViewDelegate() {
            public void onNumberToIconChanged(boolean z) {
                CollapsedStatusBarFragmentControllerNarrowNotchImpl collapsedStatusBarFragmentControllerNarrowNotchImpl = CollapsedStatusBarFragmentControllerNarrowNotchImpl.this;
                int unused = collapsedStatusBarFragmentControllerNarrowNotchImpl.mExtraSpace = z ? collapsedStatusBarFragmentControllerNarrowNotchImpl.mPhoneStatusBarContainer.getContext().getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width) : 0;
                DripStatusBarUtils.updateContainerEndMargin(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mPhoneStatusBarContainer, CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mExtraSpace);
                CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mConfigurationListener.onDensityOrFontScaleChanged();
            }
        });
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mDarkIconManager);
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mNotchLeftEarIconManager);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    public void stop() {
        Class cls = StatusBarIconController.class;
        if (this.mNotchLeftEarIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mNotchLeftEarIconManager);
        }
        if (this.mDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mDarkIconManager);
        }
        if (this.mConfigurationListener != null) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        }
        if (this.mCarrierText != null) {
            ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        }
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
    }

    public int getLayoutId() {
        if (!this.mFragment.getContext().getResources().getBoolean(R.bool.status_bar_notification_icons_notch_peeking_enabled)) {
            return R.layout.status_bar_notch_notification_enable_contents_container;
        }
        return super.getLayoutId();
    }

    public void hideSystemIconArea(boolean z, boolean z2) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null) {
            ClipEdgeLinearLayout clipEdgeLinearLayout = collapsedStatusBarFragment.mNotchLeftEarIcons;
            if (clipEdgeLinearLayout != null) {
                collapsedStatusBarFragment.animateHide(clipEdgeLinearLayout, z, z2);
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
            ClipEdgeLinearLayout clipEdgeLinearLayout = collapsedStatusBarFragment.mNotchLeftEarIcons;
            if (clipEdgeLinearLayout != null) {
                collapsedStatusBarFragment.animateShow(clipEdgeLinearLayout, z);
            }
            CarrierText carrierText = this.mCarrierText;
            if (carrierText != null) {
                carrierText.forceHide(false);
            }
        }
    }
}
