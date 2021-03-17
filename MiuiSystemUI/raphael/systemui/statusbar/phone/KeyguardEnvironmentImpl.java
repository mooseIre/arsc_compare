package com.android.systemui.statusbar.phone;

import android.service.notification.StatusBarNotification;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;

public class KeyguardEnvironmentImpl implements NotificationEntryManager.KeyguardEnvironment {
    private final DeviceProvisionedController mDeviceProvisionedController = ((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class));
    private final NotificationLockscreenUserManager mLockscreenUserManager = ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class));

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisionedController.isDeviceProvisioned();
    }

    public boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification) {
        return this.mLockscreenUserManager.isCurrentProfile(statusBarNotification.getUserId());
    }
}
