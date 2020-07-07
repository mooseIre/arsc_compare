package com.android.systemui.statusbar.phone;

import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.RegionController;
import com.android.systemui.statusbar.phone.CollapsedStatusBarFragment;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.xiaomi.stat.MiStat;

public class CollapsedStatusBarFragmentControllerImpl implements CollapsedStatusBarFragmentController, RegionController.Callback {
    private CarrierText mCarrierText;
    protected StatusBarIconController.DarkIconManager mDarkIconManager;
    protected CollapsedStatusBarFragment mFragment;
    private StatusBarIconController.IconManager mInCallIconManager;
    protected CollapsedStatusBarFragment.LeftEarIconManager mNotchLeftEarIconManager;
    private boolean mShowCarrierText;
    private boolean mShowCarrierTextForRegion;

    public int getLayoutId() {
        return R.layout.status_bar_contents_container;
    }

    public boolean isClockVisibleByPrompt(boolean z) {
        return true;
    }

    public boolean isNarrowNotch() {
        return false;
    }

    public boolean isNotch() {
        return false;
    }

    public boolean isStatusIconsVisible() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWrapContent() {
        return true;
    }

    public void init(CollapsedStatusBarFragment collapsedStatusBarFragment) {
        this.mFragment = collapsedStatusBarFragment;
    }

    public void initViews(View view) {
        adjustClockContainerWidth();
        this.mCarrierText = (CarrierText) view.findViewById(R.id.carrier);
        this.mShowCarrierText = view.getContext().getResources().getBoolean(R.bool.status_bar_show_carrier);
        updateCarrierStyle();
    }

    public void start(View view) {
        ArraySet arraySet = new ArraySet();
        arraySet.add("call_record");
        arraySet.add("mute");
        arraySet.add("speakerphone");
        this.mInCallIconManager = new StatusBarIconController.IconManager((LinearLayout) view.findViewById(R.id.call_icons));
        this.mInCallIconManager.mWhiteList = new ArraySet<>();
        this.mInCallIconManager.mWhiteList.addAll(arraySet);
        this.mDarkIconManager = new StatusBarIconController.DarkIconManager(this.mFragment.mStatusIcons);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mInCallIconManager);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    public void stop() {
        if (this.mDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
        }
        if (this.mInCallIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mInCallIconManager);
        }
        if (this.mCarrierText != null) {
            ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mCarrierText);
        }
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
    }

    public void hideSystemIconArea(boolean z, boolean z2) {
        CarrierText carrierText = this.mCarrierText;
        if (carrierText != null) {
            carrierText.forceHide(true);
        }
    }

    public void showSystemIconArea(boolean z) {
        CarrierText carrierText = this.mCarrierText;
        if (carrierText != null) {
            carrierText.forceHide(false);
        }
    }

    public boolean isGPSDriveModeVisible() {
        StatusBarIconController.DarkIconManager darkIconManager = this.mDarkIconManager;
        return darkIconManager != null && darkIconManager.hasView(MiStat.Param.LOCATION);
    }

    public void updateLeftPartVisibility(boolean z, boolean z2, boolean z3, boolean z4) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null) {
            collapsedStatusBarFragment.clockVisibleAnimate(z, !z4);
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

    private void adjustClockContainerWidth() {
        ViewGroup viewGroup;
        int i;
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null && (viewGroup = collapsedStatusBarFragment.mClockContainer) != null) {
            View findViewById = viewGroup.findViewById(R.id.clock_container);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
            if (isWrapContent()) {
                i = -2;
            } else {
                i = this.mFragment.mClockContainer.getContext().getResources().getDimensionPixelSize(R.dimen.statusbar_carrier_max_width);
            }
            layoutParams.width = i;
            findViewById.setLayoutParams(layoutParams);
        }
    }
}
