package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.statusbar.phone.AppLockHelper;
import miui.security.SecurityManager;

public class NotificationSensitiveController {
    Context mContext;
    protected SecurityManager mSecurityManager;
    UserSwitcherController mUserSwitcherController;

    static {
        boolean z = DebugConfig.DEBUG_NOTIFICATION;
    }

    public NotificationSensitiveController(Context context, UserSwitcherController userSwitcherController) {
        this.mContext = context;
        this.mUserSwitcherController = userSwitcherController;
        this.mSecurityManager = (SecurityManager) context.getSystemService("security");
    }

    public boolean showSensitive(NotificationEntry notificationEntry) {
        return showSensitiveByAppLock(notificationEntry);
    }

    public boolean showSensitiveByAppLock(NotificationEntry notificationEntry) {
        return AppLockHelper.shouldShowPublicNotificationByAppLock(this.mContext, this.mSecurityManager, notificationEntry.getSbn().getPackageName(), AppLockHelper.getCurrentUserIdIfNeeded(notificationEntry.getSbn().getUser().getIdentifier(), this.mUserSwitcherController.getCurrentUserId()));
    }
}
