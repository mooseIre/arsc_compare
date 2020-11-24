package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import android.hardware.biometrics.BiometricSourceType;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.statusbar.phone.AppLockHelper;
import miui.security.SecurityManager;

public class NotificationSensitiveController {
    private static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;
    Context mContext;
    KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    KeyguardUpdateMonitorInjector mKeyguardUpdateMonitorInjector;
    MiuiFaceUnlockManager mMiuiFaceUnlockManager;
    NotificationViewHierarchyManager mNotificationViewHierarchyManager;
    protected SecurityManager mSecurityManager;
    UserSwitcherController mUserSwitcherController;

    public NotificationSensitiveController(Context context, MiuiFaceUnlockManager miuiFaceUnlockManager, KeyguardUpdateMonitor keyguardUpdateMonitor, KeyguardUpdateMonitorInjector keyguardUpdateMonitorInjector, UserSwitcherController userSwitcherController, NotificationViewHierarchyManager notificationViewHierarchyManager) {
        this.mContext = context;
        this.mMiuiFaceUnlockManager = miuiFaceUnlockManager;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardUpdateMonitorInjector = keyguardUpdateMonitorInjector;
        this.mUserSwitcherController = userSwitcherController;
        this.mNotificationViewHierarchyManager = notificationViewHierarchyManager;
        this.mSecurityManager = (SecurityManager) context.getSystemService("security");
        registerKeyguardUpdateMonitorCallback();
    }

    private void registerKeyguardUpdateMonitorCallback() {
        this.mKeyguardUpdateMonitor.registerCallback(new KeyguardUpdateMonitorCallback() {
            public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
                if (biometricSourceType == BiometricSourceType.FACE && NotificationSensitiveController.this.mMiuiFaceUnlockManager.isShowMessageWhenFaceUnlockSuccess()) {
                    NotificationSensitiveController.this.mNotificationViewHierarchyManager.updateNotificationViews();
                }
            }
        });
    }

    public boolean showSensitive(NotificationEntry notificationEntry) {
        return showSensitiveByAppLock(notificationEntry) || showSensitiveByFaceUnlock(notificationEntry);
    }

    public boolean showSensitiveByAppLock(NotificationEntry notificationEntry) {
        return AppLockHelper.shouldShowPublicNotificationByAppLock(this.mContext, this.mSecurityManager, notificationEntry.getSbn().getPackageName(), AppLockHelper.getCurrentUserIdIfNeeded(notificationEntry.getSbn().getUser().getIdentifier(), this.mUserSwitcherController.getCurrentUserId()));
    }

    public boolean showSensitiveByFaceUnlock(NotificationEntry notificationEntry) {
        if (forceShowContent(notificationEntry)) {
            return false;
        }
        boolean isFaceUnlock = this.mKeyguardUpdateMonitorInjector.isFaceUnlock();
        boolean isShowMessageWhenFaceUnlockSuccess = this.mMiuiFaceUnlockManager.isShowMessageWhenFaceUnlockSuccess();
        if (DEBUG) {
            Log.d("NotificationSensitive", "showSensitiveByFaceUnlock faceUnlock=" + isFaceUnlock + ", sensitive=" + isShowMessageWhenFaceUnlockSuccess);
        }
        if (isFaceUnlock || !isShowMessageWhenFaceUnlockSuccess) {
            return false;
        }
        return true;
    }

    private boolean forceShowContent(NotificationEntry notificationEntry) {
        return NotificationUtil.isSystemNotification(notificationEntry.getSbn());
    }
}
