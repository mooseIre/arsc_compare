package com.android.systemui.plugins;

import android.view.View;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_OVERLAY", version = 2)
public interface OverlayPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_OVERLAY";
    public static final int VERSION = 2;

    boolean holdStatusBarOpen();

    void setCollapseDesired(boolean z);

    void setup(View view, View view2);
}
