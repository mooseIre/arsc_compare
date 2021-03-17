package com.android.systemui.shared.system;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public class PackageManagerWrapper {
    private static final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    private static final PackageManagerWrapper sInstance = new PackageManagerWrapper();

    public static PackageManagerWrapper getInstance() {
        return sInstance;
    }

    private PackageManagerWrapper() {
    }

    public ComponentName getHomeActivities(List<ResolveInfo> list) {
        try {
            return mIPackageManager.getHomeActivities(list);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResolveInfo resolveActivity(Intent intent, int i) {
        try {
            return mIPackageManager.resolveIntent(intent, intent.resolveTypeIfNeeded(AppGlobals.getInitialApplication().getContentResolver()), i, UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
