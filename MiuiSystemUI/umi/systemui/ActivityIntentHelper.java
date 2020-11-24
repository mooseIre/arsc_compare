package com.android.systemui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

public class ActivityIntentHelper {
    private final Context mContext;

    public ActivityIntentHelper(Context context) {
        this.mContext = context;
    }

    public boolean wouldLaunchResolverActivity(Intent intent, int i) {
        return getTargetActivityInfo(intent, i, false) == null;
    }

    public ActivityInfo getTargetActivityInfo(Intent intent, int i, boolean z) {
        ResolveInfo resolveActivityAsUser;
        PackageManager packageManager = this.mContext.getPackageManager();
        int i2 = !z ? 851968 : 65536;
        List queryIntentActivitiesAsUser = packageManager.queryIntentActivitiesAsUser(intent, i2, i);
        if (queryIntentActivitiesAsUser.size() == 0 || (resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, i2 | 128, i)) == null || wouldLaunchResolverActivity(resolveActivityAsUser, (List<ResolveInfo>) queryIntentActivitiesAsUser)) {
            return null;
        }
        return resolveActivityAsUser.activityInfo;
    }

    public boolean wouldShowOverLockscreen(Intent intent, int i) {
        ActivityInfo targetActivityInfo = getTargetActivityInfo(intent, i, false);
        if (targetActivityInfo == null || (targetActivityInfo.flags & 8389632) <= 0) {
            return false;
        }
        return true;
    }

    public boolean wouldLaunchResolverActivity(ResolveInfo resolveInfo, List<ResolveInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo resolveInfo2 = list.get(i);
            if (resolveInfo2.activityInfo.name.equals(resolveInfo.activityInfo.name) && resolveInfo2.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }
}
