package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;

public class KeyguardStatusBarViewControllerImpl implements KeyguardStatusBarViewController {
    protected StatusBarIconController.DarkIconManager mDarkIconManager;
    protected KeyguardStatusBarView mStatusBarView;

    public int getLayoutId() {
        return R.layout.keyguard_status_bar_system_icons_container;
    }

    public boolean isNotch() {
        return false;
    }

    public boolean isPromptCenter() {
        return false;
    }

    public void updateNotchVisible() {
    }

    public void init(KeyguardStatusBarView keyguardStatusBarView) {
        this.mStatusBarView = keyguardStatusBarView;
    }

    public void destroy() {
        this.mStatusBarView = null;
    }

    public void showStatusIcons() {
        this.mDarkIconManager = new StatusBarIconController.DarkIconManager(this.mStatusBarView.mStatusIcons, true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
    }

    public void hideStatusIcons() {
        if (this.mDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
        }
    }

    public void setDarkMode(Rect rect, float f, int i) {
        StatusBarIconController.DarkIconManager darkIconManager = this.mDarkIconManager;
        if (darkIconManager != null) {
            darkIconManager.setDarkIntensity(rect, f, i);
        }
    }
}
