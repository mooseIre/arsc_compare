package com.android.systemui.statusbar.phone;

import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.CollapsedStatusBarFragment;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.xiaomi.stat.MiStat;

public class CollapsedStatusBarFragmentControllerImpl implements CollapsedStatusBarFragmentController {
    protected StatusBarIconController.DarkIconManager mDarkIconManager;
    protected CollapsedStatusBarFragment mFragment;
    private StatusBarIconController.IconManager mInCallIconManager;
    protected CollapsedStatusBarFragment.LeftEarIconManager mNotchLeftEarIconManager;

    public int getLayoutId() {
        return R.layout.status_bar_contents_container;
    }

    public void hideSystemIconArea(boolean z, boolean z2) {
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

    public void showSystemIconArea(boolean z) {
    }

    public void init(CollapsedStatusBarFragment collapsedStatusBarFragment) {
        this.mFragment = collapsedStatusBarFragment;
    }

    public void initViews(View view) {
        adjustClockContainerWidth();
    }

    public void start(View view) {
        Class cls = StatusBarIconController.class;
        ArraySet arraySet = new ArraySet();
        arraySet.add("call_record");
        arraySet.add("mute");
        arraySet.add("speakerphone");
        StatusBarIconController.IconManager iconManager = new StatusBarIconController.IconManager((LinearLayout) view.findViewById(R.id.call_icons));
        this.mInCallIconManager = iconManager;
        iconManager.mWhiteList = new ArraySet<>();
        this.mInCallIconManager.mWhiteList.addAll(arraySet);
        this.mDarkIconManager = new StatusBarIconController.DarkIconManager(this.mFragment.mStatusIcons);
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mDarkIconManager);
        ((StatusBarIconController) Dependency.get(cls)).addIconGroup(this.mInCallIconManager);
    }

    public void stop() {
        Class cls = StatusBarIconController.class;
        if (this.mDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mDarkIconManager);
        }
        if (this.mInCallIconManager != null) {
            ((StatusBarIconController) Dependency.get(cls)).removeIconGroup(this.mInCallIconManager);
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
