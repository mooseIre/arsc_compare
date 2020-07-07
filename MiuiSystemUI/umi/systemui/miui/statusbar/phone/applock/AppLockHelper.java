package com.android.systemui.miui.statusbar.phone.applock;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.android.systemui.Constants;
import miui.security.SecurityManager;
import miui.securityspace.XSpaceUserHandle;

public class AppLockHelper {
    private static final boolean DEBUG = Constants.DEBUG;
    private static final SparseBooleanArray sACLockEnabledAsUser = new SparseBooleanArray();

    public static boolean shouldShowPublicNotificationByAppLock(Context context, SecurityManager securityManager, String str, int i) {
        boolean z = isACLockEnabledAsUser(context.getContentResolver(), i) && getApplicationAccessControlEnabledAsUser(securityManager, str, i) && getApplicationMaskNotificationEnabledAsUser(securityManager, str, i) && !checkAccessControlPassAsUser(securityManager, str, i);
        if (DEBUG) {
            Log.d("AppLockHelper", "shouldShowPublicNotificationByAppLock() lockOn=" + z + "; pkg=" + str + "; userId=" + i);
        }
        return z;
    }

    public static boolean isAppLocked(Context context, SecurityManager securityManager, String str, int i) {
        boolean z = isACLockEnabledAsUser(context.getContentResolver(), i) && getApplicationAccessControlEnabledAsUser(securityManager, str, i) && !checkAccessControlPassAsUser(securityManager, str, i);
        if (DEBUG) {
            Log.d("AppLockHelper", "isAppLocked() lockOn=" + z + "; pkg=" + str + "; userId=" + i);
        }
        return z;
    }

    public static int getCurrentUserIdIfNeeded(int i, int i2) {
        if (DEBUG) {
            Log.d("AppLockHelper", "getCurrentUserIdIfNeeded() originalUserId=" + i + "; currentUserId=" + i2);
        }
        if (i2 < 0) {
            Log.e("AppLockHelper", "getCurrentUserIdIfNeeded() error currentUserId < 0: originalUserId=" + i + "; currentUserId=" + i2);
            i2 = 0;
        }
        return i < 0 ? i2 : i;
    }

    public static void clearACLockEnabledAsUser() {
        sACLockEnabledAsUser.clear();
    }

    private static boolean isACLockEnabledAsUser(ContentResolver contentResolver, int i) {
        boolean z;
        int userIdIgnoreXspace = getUserIdIgnoreXspace(i);
        if (sACLockEnabledAsUser.indexOfKey(userIdIgnoreXspace) < 0) {
            z = Settings.Secure.getIntForUser(contentResolver, "access_control_lock_enabled", -1, userIdIgnoreXspace) == 1;
            sACLockEnabledAsUser.append(userIdIgnoreXspace, z);
        } else {
            z = sACLockEnabledAsUser.get(userIdIgnoreXspace);
        }
        if (DEBUG) {
            Log.d("AppLockHelper", String.format("isACLockEnabledAsUser userId=%d lockOn=%b", new Object[]{Integer.valueOf(userIdIgnoreXspace), Boolean.valueOf(z)}));
        }
        return z;
    }

    private static boolean getApplicationMaskNotificationEnabledAsUser(SecurityManager securityManager, String str, int i) {
        boolean applicationMaskNotificationEnabledAsUser = securityManager.getApplicationMaskNotificationEnabledAsUser(str, i);
        if (DEBUG) {
            Log.d("AppLockHelper", "getApplicationMaskNotificationEnabledAsUser() lockOn=" + applicationMaskNotificationEnabledAsUser);
        }
        return applicationMaskNotificationEnabledAsUser;
    }

    private static boolean checkAccessControlPassAsUser(SecurityManager securityManager, String str, int i) {
        boolean checkAccessControlPassAsUser = securityManager.checkAccessControlPassAsUser(str, i);
        if (DEBUG) {
            Log.d("AppLockHelper", "checkAccessControlPassAsUser() isMasked=" + checkAccessControlPassAsUser);
        }
        return checkAccessControlPassAsUser;
    }

    private static boolean getApplicationAccessControlEnabledAsUser(SecurityManager securityManager, String str, int i) {
        boolean applicationAccessControlEnabledAsUser = securityManager.getApplicationAccessControlEnabledAsUser(str, i);
        if (DEBUG) {
            Log.d("AppLockHelper", "getApplicationAccessControlEnabledAsUser() lockOn=" + applicationAccessControlEnabledAsUser);
        }
        return applicationAccessControlEnabledAsUser;
    }

    private static int getUserIdIgnoreXspace(int i) {
        if (XSpaceUserHandle.isXSpaceUserId(i)) {
            return 0;
        }
        return i;
    }
}
