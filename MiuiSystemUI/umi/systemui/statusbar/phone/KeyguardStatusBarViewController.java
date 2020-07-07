package com.android.systemui.statusbar.phone;

import android.graphics.Rect;

public interface KeyguardStatusBarViewController {
    void destroy();

    int getLayoutId();

    void hideStatusIcons();

    void init(KeyguardStatusBarView keyguardStatusBarView);

    boolean isNotch();

    boolean isPromptCenter();

    void setDarkMode(Rect rect, float f, int i);

    void showStatusIcons();

    void updateNotchVisible();
}
