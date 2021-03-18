package com.android.systemui.util.magnetictarget;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

/* compiled from: MagnetizedObject.kt */
public final class MagnetizedObject$Companion$initHapticSettingObserver$hapticSettingObserver$1 extends ContentObserver {
    final /* synthetic */ Context $context;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MagnetizedObject$Companion$initHapticSettingObserver$hapticSettingObserver$1(Context context, Handler handler) {
        super(handler);
        this.$context = context;
    }

    public void onChange(boolean z) {
        boolean z2 = false;
        if (Settings.System.getIntForUser(this.$context.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0) {
            z2 = true;
        }
        MagnetizedObject.systemHapticsEnabled = z2;
    }
}
