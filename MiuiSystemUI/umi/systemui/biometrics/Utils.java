package com.android.systemui.biometrics;

import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.widget.LockPatternUtils;

public class Utils {
    static void notifyAccessibilityContentChanged(AccessibilityManager accessibilityManager, ViewGroup viewGroup) {
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain();
            obtain.setEventType(2048);
            obtain.setContentChangeTypes(1);
            viewGroup.sendAccessibilityEventUnchecked(obtain);
            viewGroup.notifySubtreeAccessibilityStateChanged(viewGroup, viewGroup, 1);
        }
    }

    static boolean isDeviceCredentialAllowed(Bundle bundle) {
        return (getAuthenticators(bundle) & 32768) != 0;
    }

    static boolean isBiometricAllowed(Bundle bundle) {
        return (getAuthenticators(bundle) & 255) != 0;
    }

    static int getAuthenticators(Bundle bundle) {
        return bundle.getInt("authenticators_allowed");
    }

    static int getCredentialType(Context context, int i) {
        int keyguardStoredPasswordQuality = new LockPatternUtils(context).getKeyguardStoredPasswordQuality(i);
        if (keyguardStoredPasswordQuality != 65536) {
            return (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) ? 1 : 3;
        }
        return 2;
    }

    static boolean isManagedProfile(Context context, int i) {
        return ((UserManager) context.getSystemService(UserManager.class)).isManagedProfile(i);
    }
}
