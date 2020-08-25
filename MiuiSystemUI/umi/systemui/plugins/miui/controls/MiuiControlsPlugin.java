package com.android.systemui.plugins.miui.controls;

import android.view.View;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_MIUI_CONTROLS", version = 1)
public interface MiuiControlsPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_MIUI_CONTROLS";
    public static final int VERSION = 1;

    View getControlsView();

    void hideControlsView();
}
