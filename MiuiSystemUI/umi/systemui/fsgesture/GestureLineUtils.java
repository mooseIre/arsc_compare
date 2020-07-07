package com.android.systemui.fsgesture;

import android.content.Context;
import android.provider.Settings;
import android.widget.RelativeLayout;
import com.android.systemui.Application;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.phone.NavigationHandle;

public class GestureLineUtils {
    public static boolean isShowNavigationHandle(Context context) {
        Recents recents = (Recents) ((Application) context.getApplicationContext()).getSystemUIApplication().getComponent(Recents.class);
        return recents != null && recents.useFsGestureVersionThree() && !isHideGestureLine(context);
    }

    private static boolean isHideGestureLine(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "hide_gesture_line", 0) != 0;
    }

    public static void updateNavigationHandleVisibility(Context context, NavigationHandle navigationHandle) {
        if (context != null && navigationHandle != null) {
            navigationHandle.setVisibility(isShowNavigationHandle(context) ? 0 : 4);
        }
    }

    public static NavigationHandle createAndaddNavigationHandle(RelativeLayout relativeLayout) {
        NavigationHandle navigationHandle = new NavigationHandle(relativeLayout.getContext());
        navigationHandle.setColor(!((relativeLayout.getContext().getResources().getConfiguration().uiMode & 32) == 32));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, relativeLayout.getContext().getResources().getDimensionPixelSize(17105307));
        layoutParams.addRule(12);
        layoutParams.addRule(14);
        relativeLayout.addView(navigationHandle, layoutParams);
        return navigationHandle;
    }
}
