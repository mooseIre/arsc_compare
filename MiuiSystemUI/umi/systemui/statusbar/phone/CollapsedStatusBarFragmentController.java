package com.android.systemui.statusbar.phone;

import android.view.View;

public interface CollapsedStatusBarFragmentController {
    int getLayoutId();

    void hideSystemIconArea(boolean z, boolean z2);

    void init(CollapsedStatusBarFragment collapsedStatusBarFragment);

    void initViews(View view);

    boolean isClockVisibleByPrompt(boolean z);

    boolean isGPSDriveModeVisible();

    boolean isNarrowNotch();

    boolean isNotch();

    boolean isStatusIconsVisible();

    void showSystemIconArea(boolean z);

    void start(View view);

    void stop();

    void updateLeftPartVisibility(boolean z, boolean z2, boolean z3, boolean z4);
}
