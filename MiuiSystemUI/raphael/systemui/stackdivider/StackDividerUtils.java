package com.android.systemui.stackdivider;

import android.content.res.Configuration;
import java.util.ArrayList;
import java.util.List;

public class StackDividerUtils {
    public static final List<String> sSplitScreenFroceNotResizePkgList = new ArrayList();

    public static boolean canBeDividerResized(String str) {
        return !sSplitScreenFroceNotResizePkgList.contains(str);
    }

    public static void updateSplitScreenFroceNotResizePkgList(List<String> list, boolean z) {
        if (z) {
            sSplitScreenFroceNotResizePkgList.addAll(list);
        } else {
            sSplitScreenFroceNotResizePkgList.removeAll(list);
        }
    }

    public static boolean isWideScreen(Configuration configuration) {
        return configuration != null && configuration.smallestScreenWidthDp >= 600;
    }
}
