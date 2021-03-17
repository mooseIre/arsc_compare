package com.android.systemui.statusbar.notification;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;

public class NotificationFilter {
    private NotificationEntryManager.KeyguardEnvironment mEnvironment;
    private ForegroundServiceController mFsc;
    private final Boolean mIsMediaFlagEnabled = Boolean.TRUE;
    private final StatusBarStateController mStatusBarStateController;
    private NotificationLockscreenUserManager mUserManager;

    public NotificationFilter(StatusBarStateController statusBarStateController, MediaFeatureFlag mediaFeatureFlag) {
        NotificationGroupManager notificationGroupManager = (NotificationGroupManager) Dependency.get(NotificationGroupManager.class);
        this.mStatusBarStateController = statusBarStateController;
        mediaFeatureFlag.getEnabled();
    }

    private NotificationEntryManager.KeyguardEnvironment getEnvironment() {
        if (this.mEnvironment == null) {
            this.mEnvironment = (NotificationEntryManager.KeyguardEnvironment) Dependency.get(NotificationEntryManager.KeyguardEnvironment.class);
        }
        return this.mEnvironment;
    }

    private ForegroundServiceController getFsc() {
        if (this.mFsc == null) {
            this.mFsc = (ForegroundServiceController) Dependency.get(ForegroundServiceController.class);
        }
        return this.mFsc;
    }

    private NotificationLockscreenUserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = (NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class);
        }
        return this.mUserManager;
    }

    public boolean shouldFilterOut(NotificationEntry notificationEntry) {
        String[] stringArray;
        ExpandedNotification sbn = notificationEntry.getSbn();
        if ((!getEnvironment().isDeviceProvisioned() && !showNotificationEvenIfUnprovisioned(sbn)) || !getEnvironment().isNotificationForCurrentProfiles(sbn)) {
            return true;
        }
        if (getUserManager().isLockscreenPublicMode(sbn.getUserId()) && (sbn.getNotification().visibility == -1 || getUserManager().shouldHideNotifications(sbn.getUserId()) || getUserManager().shouldHideNotifications(sbn.getKey()))) {
            return true;
        }
        if (this.mStatusBarStateController.isDozing() && notificationEntry.shouldSuppressAmbient()) {
            return true;
        }
        if ((!this.mStatusBarStateController.isDozing() && notificationEntry.shouldSuppressNotificationList()) || notificationEntry.getRanking().isSuspended()) {
            return true;
        }
        if (getFsc().isDisclosureNotification(sbn) && !getFsc().isDisclosureNeededForUser(sbn.getUserId())) {
            return true;
        }
        if (getFsc().isSystemAlertNotification(sbn) && (stringArray = sbn.getNotification().extras.getStringArray("android.foregroundApps")) != null && stringArray.length >= 1 && !getFsc().isSystemAlertWarningNeeded(sbn.getUserId(), stringArray[0])) {
            return true;
        }
        if (!this.mIsMediaFlagEnabled.booleanValue() || !MediaDataManagerKt.isMediaNotification(sbn)) {
            return NotificationFilterController.shouldFilterOut(notificationEntry);
        }
        return true;
    }

    private static boolean showNotificationEvenIfUnprovisioned(StatusBarNotification statusBarNotification) {
        return showNotificationEvenIfUnprovisioned(AppGlobals.getPackageManager(), statusBarNotification);
    }

    @VisibleForTesting
    static boolean showNotificationEvenIfUnprovisioned(IPackageManager iPackageManager, StatusBarNotification statusBarNotification) {
        return checkUidPermission(iPackageManager, "android.permission.NOTIFICATION_DURING_SETUP", statusBarNotification.getUid()) == 0 && statusBarNotification.getNotification().extras.getBoolean("android.allowDuringSetup");
    }

    private static int checkUidPermission(IPackageManager iPackageManager, String str, int i) {
        try {
            return iPackageManager.checkUidPermission(str, i);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
