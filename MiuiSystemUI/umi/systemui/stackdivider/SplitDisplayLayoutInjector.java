package com.android.systemui.stackdivider;

import android.content.Context;

public class SplitDisplayLayoutInjector {
    public static boolean canUpdatePrimarySplitSide(Context context) {
        return context != null && StackDividerUtils.isWideScreen(context.getResources().getConfiguration());
    }

    public static int getPrimarySplitSide(Context context, boolean z) {
        return ((context == null || !StackDividerUtils.isWideScreen(context.getResources().getConfiguration())) && !z) ? 2 : 1;
    }
}
