package com.android.systemui.stackdivider;

import android.content.Context;
import android.content.res.Configuration;
import com.android.systemui.C0008R$array;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import java.util.Arrays;

public class DividerInjector {
    public static void updateSplitScreenFroceNotResizePkgList(Context context) {
        if (context != null && StackDividerUtils.isWideScreen(context.getResources().getConfiguration())) {
            StackDividerUtils.updateSplitScreenFroceNotResizePkgList(Arrays.asList(context.getResources().getStringArray(C0008R$array.split_screen_force_not_resize_pkg_list)), true);
        }
    }

    public static boolean getOrientationIfNeed(Configuration configuration, boolean z) {
        if (configuration == null || !StackDividerUtils.isWideScreen(configuration)) {
            return z;
        }
        return true;
    }

    public static DividerSnapAlgorithm.SnapTarget updateSnapTargetIfNeed(DividerSnapAlgorithm.SnapTarget snapTarget, Context context, DividerSnapAlgorithm dividerSnapAlgorithm) {
        return (context == null || !StackDividerUtils.isWideScreen(context.getResources().getConfiguration())) ? snapTarget : dividerSnapAlgorithm.getMiddleTarget();
    }
}
