package com.android.systemui.statusbar.phone;

import android.util.ArraySet;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.CollapsedStatusBarFragment;
import java.util.Objects;

public class CollapsedStatusBarFragmentControllerNotchImpl extends CollapsedStatusBarFragmentControllerImpl {
    public boolean isClockVisibleByPrompt(boolean z) {
        return z;
    }

    public boolean isNarrowNotch() {
        return false;
    }

    public boolean isNotch() {
        return true;
    }

    public boolean isStatusIconsVisible() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isWrapContent() {
        return false;
    }

    public int getLayoutId() {
        if (!this.mFragment.getContext().getResources().getBoolean(R.bool.status_bar_notification_icons_notch_peeking_enabled)) {
            return R.layout.status_bar_notch_notification_enable_contents_container;
        }
        return super.getLayoutId();
    }

    public void initViews(View view) {
        ArraySet<String> arraySet = this.mFragment.mNotchleftearIconsList;
        arraySet.add("bluetooth");
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        Objects.requireNonNull(collapsedStatusBarFragment);
        CollapsedStatusBarFragment.LeftEarIconManager leftEarIconManager = new CollapsedStatusBarFragment.LeftEarIconManager(this.mFragment.mNotchLeftEarIcons);
        this.mNotchLeftEarIconManager = leftEarIconManager;
        leftEarIconManager.mWhiteList = new ArraySet<>();
        this.mNotchLeftEarIconManager.mWhiteList.addAll(arraySet);
    }

    public void start(View view) {
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mNotchLeftEarIconManager);
    }

    public void stop() {
        if (this.mNotchLeftEarIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mNotchLeftEarIconManager);
        }
    }
}
