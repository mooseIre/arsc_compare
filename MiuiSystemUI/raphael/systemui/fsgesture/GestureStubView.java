package com.android.systemui.fsgesture;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.provider.Settings;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;

public class GestureStubView extends FrameLayout {
    private static boolean isUserSetUp;

    static boolean supportNextTask(KeyguardManager keyguardManager, ContentResolver contentResolver) {
        return !keyguardManager.isKeyguardLocked() && isUserSetUp(contentResolver);
    }

    private static boolean isUserSetUp(ContentResolver contentResolver) {
        if (!isUserSetUp) {
            boolean z = false;
            if (!(Settings.Global.getInt(contentResolver, "device_provisioned", 0) == 0 || Settings.Secure.getIntForUser(contentResolver, "user_setup_complete", 0, KeyguardUpdateMonitor.getCurrentUser()) == 0)) {
                z = true;
            }
            isUserSetUp = z;
        }
        return isUserSetUp;
    }
}
