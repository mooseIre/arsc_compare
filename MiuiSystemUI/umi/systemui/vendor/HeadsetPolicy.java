package com.android.systemui.vendor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import com.android.systemui.Dependency;

public class HeadsetPolicy {
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            updateHeadset(intent);
        }

        private void updateHeadset(Intent intent) {
            boolean z = false;
            if (intent.getIntExtra("state", 0) == 1) {
                z = true;
            }
            if (z && !HeadsetPolicy.this.mPowerManager.isScreenOn()) {
                HeadsetPolicy.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:HEADSET");
            }
        }
    };
    /* access modifiers changed from: private */
    public final PowerManager mPowerManager;

    public HeadsetPolicy(Context context) {
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        Handler handler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        context.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, (String) null, handler);
    }
}
