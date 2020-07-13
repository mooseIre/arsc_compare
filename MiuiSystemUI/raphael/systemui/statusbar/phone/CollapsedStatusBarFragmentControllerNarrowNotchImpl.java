package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
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
        ArraySet<String> arraySet = this.mFragment.mNotchleftearIconsList;
        arraySet.add("bluetooth");
        arraySet.add(MiStat.Param.LOCATION);
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        Objects.requireNonNull(collapsedStatusBarFragment);
        this.mNotchLeftEarIconManager = new CollapsedStatusBarFragment.LeftEarIconManager(this.mFragment.mNotchLeftEarIcons);
        this.mNotchLeftEarIconManager.mWhiteList = new ArraySet<>();
        this.mNotchLeftEarIconManager.mWhiteList.addAll(arraySet);
        this.mDarkIconManager = new StatusBarIconController.DarkIconManager(this.mFragment.mStatusIcons);
        this.mDarkIconManager.mWhiteList = new ArraySet<>();
        this.mDarkIconManager.mWhiteList.add("volume");
        this.mDarkIconManager.mWhiteList.add("quiet");
        this.mDarkIconManager.mWhiteList.add("alarm_clock");
        this.mSystemIconArea = view.findViewById(R.id.system_icons);
        this.mConfigurationListener = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mFragment.mClockContainer, true, false, 0);
                DripStatusBarUtils.updateContainerWidth(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mSystemIconArea, false, false, CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mExtraSpace);
            }
        };
        this.mConfigurationListener.onDensityOrFontScaleChanged();
        this.mPhoneStatusBarContainer = (ViewGroup) view.findViewById(R.id.phone_status_bar_contents_container);
        ((BatteryMeterView) this.mSystemIconArea.findViewById(R.id.battery)).setBatteryMeterViewDelegate(new BatteryMeterView.BatteryMeterViewDelegate() {
            public void onNumberToIconChanged(boolean z) {
                CollapsedStatusBarFragmentControllerNarrowNotchImpl collapsedStatusBarFragmentControllerNarrowNotchImpl = CollapsedStatusBarFragmentControllerNarrowNotchImpl.this;
                int unused = collapsedStatusBarFragmentControllerNarrowNotchImpl.mExtraSpace = z ? collapsedStatusBarFragmentControllerNarrowNotchImpl.mPhoneStatusBarContainer.getContext().getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width) : 0;
                DripStatusBarUtils.updateContainerEndMargin(CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mPhoneStatusBarContainer, CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mExtraSpace);
                CollapsedStatusBarFragmentControllerNarrowNotchImpl.this.mConfigurationListener.onDensityOrFontScaleChanged();
            }
        });
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mNotchLeftEarIconManager);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    public void stop() {
        if (this.mNotchLeftEarIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mNotchLeftEarIconManager);
        }
        if (this.mDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
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
}
