package com.android.systemui.plugins;

import android.view.View;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.plugins.statusbar.DozeParameters;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_OVERLAY", version = 4)
public interface OverlayPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_OVERLAY";
    public static final int VERSION = 4;

    public interface Callback {
        void onHoldStatusBarOpenChange();
    }

    boolean holdStatusBarOpen() {
        return false;
    }

    void setCollapseDesired(boolean z) {
    }

    void setup(View view, View view2);

    void setup(View view, View view2, Callback callback, DozeParameters dozeParameters) {
        setup(view, view2);
    }
}
