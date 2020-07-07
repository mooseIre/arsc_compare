package com.android.systemui.statusbar.phone;

import com.android.systemui.plugins.R;

public class KeyguardStatusBarViewControllerNotchImpl extends KeyguardStatusBarViewControllerImpl {
    public boolean isNotch() {
        return true;
    }

    public boolean isPromptCenter() {
        return true;
    }

    public void showStatusIcons() {
    }

    public void init(KeyguardStatusBarView keyguardStatusBarView) {
        super.init(keyguardStatusBarView);
        keyguardStatusBarView.findViewById(R.id.statusIcons).setVisibility(8);
    }
}
