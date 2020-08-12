package com.android.systemui.recents.model;

import android.content.Context;
import android.os.UserHandle;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;

public class RecentsPackageMonitor extends PackageMonitor {
    public void register(Context context) {
        try {
            register(context, BackgroundThread.get().getLooper(), UserHandle.ALL, true);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        try {
            RecentsPackageMonitor.super.unregister();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void onPackageRemoved(String str, int i) {
        RecentsEventBus.getDefault().post(new PackagesChangedEvent(this, str, getChangingUserId()));
    }

    public boolean onPackageChanged(String str, int i, String[] strArr) {
        onPackageModified(str);
        return true;
    }

    public void onPackageModified(String str) {
        RecentsEventBus.getDefault().post(new PackagesChangedEvent(this, str, getChangingUserId()));
    }
}
