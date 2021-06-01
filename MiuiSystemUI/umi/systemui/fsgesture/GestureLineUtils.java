package com.android.systemui.fsgesture;

import android.content.Context;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.widget.RelativeLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.phone.NavigationHandle;

public class GestureLineUtils {
    public static boolean isShowNavigationHandle(Context context) {
        return MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar") && !isHideGestureLine(context);
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(relativeLayout.getContext().getResources().getDimensionPixelSize(C0012R$dimen.navigation_home_handle_width), relativeLayout.getContext().getResources().getDimensionPixelSize(17105336));
        layoutParams.addRule(12);
        layoutParams.addRule(14);
        relativeLayout.addView(navigationHandle, layoutParams);
        return navigationHandle;
    }
}
