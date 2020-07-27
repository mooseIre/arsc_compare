package com.android.systemui.util.wakelock;

import android.content.Context;
import android.os.PowerManager;

public abstract class WakeLockHelper implements WakeLock {
    public static WakeLock createPartial(Context context, String str) {
        return wrap(createPartialInner(context, str));
    }

    static PowerManager.WakeLock createPartialInner(Context context, String str) {
        return ((PowerManager) context.getSystemService(PowerManager.class)).newWakeLock(1, str);
    }

    static WakeLock wrap(final PowerManager.WakeLock wakeLock) {
        return new WakeLock() {
            public void acquire() {
                wakeLock.acquire();
            }

            public void release() {
                wakeLock.release();
            }
        };
    }
}
