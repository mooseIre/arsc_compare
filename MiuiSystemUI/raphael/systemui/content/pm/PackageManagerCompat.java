package com.android.systemui.content.pm;

import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import java.util.List;

public class PackageManagerCompat {
    public static PackageInfo getPackageInfoAsUser(PackageManager packageManager, String str, int i, int i2) throws PackageManager.NameNotFoundException {
        return packageManager.getPackageInfoAsUser(str, i, i2);
    }

    public static boolean hasSystemFeature(IPackageManager iPackageManager, String str, int i) throws RemoteException {
        return iPackageManager.hasSystemFeature(str, i);
    }

    public static List<ResolveInfo> queryBroadcastReceiversAsUser(PackageManager packageManager, Intent intent, int i, int i2) {
        return packageManager.queryBroadcastReceiversAsUser(intent, i, -2);
    }
}
