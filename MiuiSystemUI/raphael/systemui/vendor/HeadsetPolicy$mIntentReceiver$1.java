package com.android.systemui.vendor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: HeadsetPolicy.kt */
public final class HeadsetPolicy$mIntentReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ HeadsetPolicy this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    HeadsetPolicy$mIntentReceiver$1(HeadsetPolicy headsetPolicy) {
        this.this$0 = headsetPolicy;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        updateHeadset(intent);
    }

    private final void updateHeadset(Intent intent) {
        boolean z = false;
        if (intent.getIntExtra("state", 0) == 1) {
            z = true;
        }
        if (z && !this.this$0.mPowerManager.isScreenOn()) {
            this.this$0.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:HEADSET");
        }
    }
}
