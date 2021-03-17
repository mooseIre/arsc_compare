package com.android.systemui.stackdivider;

import android.view.View;

public class DividerViewInjector {
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        r0 = r0.topActivity;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean canBeDividerResized(android.view.View r0, com.android.systemui.stackdivider.SplitScreenTaskOrganizer r1) {
        /*
            if (r0 == 0) goto L_0x0038
            if (r1 == 0) goto L_0x0038
            android.content.res.Resources r0 = r0.getResources()
            android.content.res.Configuration r0 = r0.getConfiguration()
            boolean r0 = com.android.systemui.stackdivider.StackDividerUtils.isWideScreen(r0)
            if (r0 == 0) goto L_0x0038
            android.app.ActivityManager$RunningTaskInfo r0 = r1.mPrimary
            if (r0 == 0) goto L_0x0024
            android.content.ComponentName r0 = r0.topActivity
            if (r0 == 0) goto L_0x0024
            java.lang.String r0 = r0.getPackageName()
            boolean r0 = com.android.systemui.stackdivider.StackDividerUtils.canBeDividerResized(r0)
            if (r0 == 0) goto L_0x0036
        L_0x0024:
            android.app.ActivityManager$RunningTaskInfo r0 = r1.mSecondary
            if (r0 == 0) goto L_0x0038
            android.content.ComponentName r0 = r0.topActivity
            if (r0 == 0) goto L_0x0038
            java.lang.String r0 = r0.getPackageName()
            boolean r0 = com.android.systemui.stackdivider.StackDividerUtils.canBeDividerResized(r0)
            if (r0 != 0) goto L_0x0038
        L_0x0036:
            r0 = 0
            return r0
        L_0x0038:
            r0 = 1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.DividerViewInjector.canBeDividerResized(android.view.View, com.android.systemui.stackdivider.SplitScreenTaskOrganizer):boolean");
    }

    public static boolean canUpdateDivisionPosition(View view) {
        return view != null && StackDividerUtils.isWideScreen(view.getResources().getConfiguration());
    }

    public static boolean isHorizontalDivision(View view) {
        return view != null && !StackDividerUtils.isWideScreen(view.getResources().getConfiguration());
    }
}
