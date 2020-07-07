package com.android.keyguard;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.systemui.Application;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.miui.ActivityObserver;
import com.miui.systemui.annotation.Inject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MiuiFastUnlockController {
    private final ArrayList<WeakReference<FastUnlockCallback>> mCallbacks = new ArrayList<>();
    private Context mContext;
    private volatile boolean mFastUnlock = false;
    private PowerManager mPowerManager;

    public interface FastUnlockCallback {
        void onFinishFastUnlock() {
        }

        void onStartFastUnlock() {
        }
    }

    public MiuiFastUnlockController(@Inject Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    public boolean fastUnlock() {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(KeyguardViewMediator.class);
        if (!supportFastUnlock() || keyguardViewMediator == null) {
            return false;
        }
        onStartFastUnlock();
        this.mFastUnlock = true;
        hideKeygaurdFast(keyguardViewMediator);
        onFinishFashUnlock();
        return true;
    }

    public boolean wakeAndFastUnlock(String str) {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(KeyguardViewMediator.class);
        if (!supportFastUnlock() || keyguardViewMediator == null) {
            return false;
        }
        onStartFastUnlock();
        this.mFastUnlock = true;
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            hideKeygaurdFast(keyguardViewMediator);
            wakeupIfNeed(str);
        } else {
            wakeupIfNeed(str);
            hideKeygaurdFast(keyguardViewMediator);
        }
        onFinishFashUnlock();
        return true;
    }

    public boolean isFastUnlock() {
        return this.mFastUnlock;
    }

    public void resetFastUnlockState() {
        this.mFastUnlock = false;
    }

    public void registerCallback(FastUnlockCallback fastUnlockCallback) {
        int i = 0;
        while (i < this.mCallbacks.size()) {
            if (this.mCallbacks.get(i).get() != fastUnlockCallback) {
                i++;
            } else {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(fastUnlockCallback));
        removeCallback((FastUnlockCallback) null);
    }

    public void removeCallback(FastUnlockCallback fastUnlockCallback) {
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == fastUnlockCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }

    private void onStartFastUnlock() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            FastUnlockCallback fastUnlockCallback = (FastUnlockCallback) this.mCallbacks.get(i).get();
            if (fastUnlockCallback != null) {
                fastUnlockCallback.onStartFastUnlock();
            }
        }
    }

    private void onFinishFashUnlock() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            FastUnlockCallback fastUnlockCallback = (FastUnlockCallback) this.mCallbacks.get(i).get();
            if (fastUnlockCallback != null) {
                fastUnlockCallback.onFinishFastUnlock();
            }
        }
    }

    private void hideKeygaurdFast(KeyguardViewMediator keyguardViewMediator) {
        keyguardViewMediator.preHideKeyguard();
    }

    private void wakeupIfNeed(String str) {
        if (!KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive()) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), str);
        }
    }

    private boolean supportFastUnlock() {
        return (MiuiKeyguardUtils.isGxzwSensor() || MiuiKeyguardUtils.isBroadSideFingerprint()) && (MiuiKeyguardUtils.isWeakenAimationEnable(this.mContext) || (!KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive() && ((ActivityObserver) Dependency.get(ActivityObserver.class)).isTopActivityLauncher()));
    }
}
