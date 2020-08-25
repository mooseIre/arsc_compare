package com.android.systemui.miui.analytics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.systemui.Constants;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.MiStatParams;
import miui.os.Build;

public class AnalyticsWrapper {
    public static boolean sSupprotAggregate = false;

    static String resolveChannelName() {
        String str;
        if (Build.IS_ALPHA_BUILD) {
            str = "MIUI10-alpha";
        } else {
            str = Build.IS_DEVELOPMENT_VERSION ? "MIUI10-dev" : "MIUI10";
        }
        if (!sSupprotAggregate) {
            return str;
        }
        return str + "-12pre";
    }

    private static void initSupportAggregate(Context context) {
        String str;
        boolean z = false;
        long j = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.miui.notification", 0);
            j = packageInfo.getLongVersionCode();
            str = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            str = "";
        }
        if (str.contains("beta")) {
            if (j > 1010113) {
                z = true;
            }
            sSupprotAggregate = z;
            return;
        }
        if (j > 1010013) {
            z = true;
        }
        sSupprotAggregate = z;
    }

    public static void init(Context context) {
        initSupportAggregate(context);
        MiStat.initialize(context, "2882303761517402087", "5581740267087", false, resolveChannelName());
        MiStat.setUploadNetworkType(Constants.IS_INDIA_REGION || Constants.isIndiaDevice() ? 31 : 8);
        MiStat.setUseSystemUploadingService(true, true);
        MiStat.setExceptionCatcherEnabled(false);
        MiStat.setDebugModeEnabled(Constants.DEBUG);
        MiStat.setUserProperty("support_aggregate", String.valueOf(sSupprotAggregate));
    }

    public static void trackEvent(String str) {
        MiStat.trackEvent(str);
    }

    public static void trackEvent(String str, String str2) {
        MiStat.trackEvent(str, str2);
    }

    public static void trackEvent(String str, MiStatParams miStatParams) {
        MiStat.trackEvent(str, miStatParams);
    }

    public static void trackPlainTextEvent(String str, String str2) {
        MiStat.trackPlainTextEvent(str, str2);
    }

    public static void setUserProperty(MiStatParams miStatParams) {
        MiStat.setUserProperty(miStatParams);
    }
}
