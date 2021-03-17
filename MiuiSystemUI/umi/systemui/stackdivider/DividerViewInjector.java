package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.view.View;

public class DividerViewInjector {
    public static boolean canBeDividerResized(View view, SplitScreenTaskOrganizer splitScreenTaskOrganizer) {
        ComponentName componentName;
        ComponentName componentName2;
        if (view == null || splitScreenTaskOrganizer == null || !StackDividerUtils.isWideScreen(view.getResources().getConfiguration())) {
            return true;
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = splitScreenTaskOrganizer.mPrimary;
        if (runningTaskInfo != null && (componentName2 = runningTaskInfo.topActivity) != null && !StackDividerUtils.canBeDividerResized(componentName2.getPackageName())) {
            return false;
        }
        ActivityManager.RunningTaskInfo runningTaskInfo2 = splitScreenTaskOrganizer.mSecondary;
        return runningTaskInfo2 == null || (componentName = runningTaskInfo2.topActivity) == null || StackDividerUtils.canBeDividerResized(componentName.getPackageName());
    }

    public static boolean canUpdateDivisionPosition(View view) {
        return view != null && StackDividerUtils.isWideScreen(view.getResources().getConfiguration());
    }

    public static boolean isHorizontalDivision(View view) {
        return view != null && !StackDividerUtils.isWideScreen(view.getResources().getConfiguration());
    }
}
