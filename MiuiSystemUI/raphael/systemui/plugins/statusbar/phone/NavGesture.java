package com.android.systemui.plugins.statusbar.phone;

import android.view.MotionEvent;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_NAV_GESTURE", version = 1)
public interface NavGesture extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NAV_GESTURE";
    public static final int VERSION = 1;

    public interface GestureHelper {
        void destroy();

        boolean onInterceptTouchEvent(MotionEvent motionEvent);

        boolean onTouchEvent(MotionEvent motionEvent);

        void setBarState(boolean z, boolean z2);
    }

    GestureHelper getGestureHelper();
}
