package com.android.systemui.plugins;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = NavigationEdgeBackPlugin.ACTION, version = 1)
public interface NavigationEdgeBackPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NAVIGATION_EDGE_BACK_ACTION";
    public static final int VERSION = 1;

    public interface BackCallback {
        void cancelBack();

        void triggerBack();
    }

    void onMotionEvent(MotionEvent motionEvent);

    void setBackCallback(BackCallback backCallback);

    void setDisplaySize(Point point);

    void setInsets(int i, int i2);

    void setIsLeftPanel(boolean z);

    void setLayoutParams(WindowManager.LayoutParams layoutParams);
}
