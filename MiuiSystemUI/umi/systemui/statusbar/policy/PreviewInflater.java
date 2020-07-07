package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

public class PreviewInflater {
    public static boolean wouldLaunchResolverActivity(Context context, Intent intent, int i) {
        return getTargetActivityInfo(context, intent, i, false) == null;
    }

    public static ActivityInfo getTargetActivityInfo(Context context, Intent intent, int i, boolean z) {
        ResolveInfo resolveActivityAsUser;
        PackageManager packageManager = context.getPackageManager();
        int i2 = !z ? 851968 : 65536;
        List queryIntentActivitiesAsUser = packageManager.queryIntentActivitiesAsUser(intent, i2, i);
        if (queryIntentActivitiesAsUser.size() == 0 || (resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, i2 | 128, i)) == null || wouldLaunchResolverActivity(resolveActivityAsUser, queryIntentActivitiesAsUser)) {
            return null;
        }
        return resolveActivityAsUser.activityInfo;
    }

    private static boolean wouldLaunchResolverActivity(ResolveInfo resolveInfo, List<ResolveInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo resolveInfo2 = list.get(i);
            if (resolveInfo2.activityInfo.name.equals(resolveInfo.activityInfo.name) && resolveInfo2.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }
}
