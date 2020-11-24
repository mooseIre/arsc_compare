package com.android.systemui.statusbar.notification.collection.coordinator;

import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;

public class DeviceProvisionedCoordinator implements Coordinator {
    /* access modifiers changed from: private */
    public final DeviceProvisionedController mDeviceProvisionedController;
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedListener = new DeviceProvisionedController.DeviceProvisionedListener() {
        public void onDeviceProvisionedChanged() {
            DeviceProvisionedCoordinator.this.mNotifFilter.invalidateList();
        }
    };
    private final IPackageManager mIPackageManager;
    /* access modifiers changed from: private */
    public final NotifFilter mNotifFilter = new NotifFilter("DeviceProvisionedCoordinator") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return !DeviceProvisionedCoordinator.this.mDeviceProvisionedController.isDeviceProvisioned() && !DeviceProvisionedCoordinator.this.showNotificationEvenIfUnprovisioned(notificationEntry.getSbn());
        }
    };

    public DeviceProvisionedCoordinator(DeviceProvisionedController deviceProvisionedController, IPackageManager iPackageManager) {
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mIPackageManager = iPackageManager;
    }

    public void attach(NotifPipeline notifPipeline) {
        this.mDeviceProvisionedController.addCallback(this.mDeviceProvisionedListener);
        notifPipeline.addPreGroupFilter(this.mNotifFilter);
    }

    /* access modifiers changed from: private */
    public boolean showNotificationEvenIfUnprovisioned(StatusBarNotification statusBarNotification) {
        if (!(checkUidPermission("android.permission.NOTIFICATION_DURING_SETUP", statusBarNotification.getUid()) == 0) || !statusBarNotification.getNotification().extras.getBoolean("android.allowDuringSetup")) {
            return false;
        }
        return true;
    }

    private int checkUidPermission(String str, int i) {
        try {
            return this.mIPackageManager.checkUidPermission(str, i);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
