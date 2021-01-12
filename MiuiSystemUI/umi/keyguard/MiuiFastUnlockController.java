package com.android.keyguard;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.injector.KeyguardViewMediatorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MiuiFastUnlockController {
    private final ArrayList<WeakReference<FastUnlockCallback>> mCallbacks = new ArrayList<>();
    private Context mContext;
    private volatile boolean mFastUnlock = false;
    private KeyguardViewMediator mKeyguardViewMediator;
    private PowerManager mPowerManager;

    public interface FastUnlockCallback {
        void onFinishFastUnlock() {
        }

        void onStartFastUnlock() {
        }
    }

    public MiuiFastUnlockController(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mKeyguardViewMediator = keyguardViewMediator;
    }

    public boolean wakeAndFastUnlock(String str) {
        if (!supportFastUnlock() || this.mKeyguardViewMediator == null) {
            return false;
        }
        onStartFastUnlock();
        this.mFastUnlock = true;
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("fp", true);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            hideKeygaurdFast(this.mKeyguardViewMediator);
            wakeupIfNeed(str);
        } else {
            wakeupIfNeed(str);
            hideKeygaurdFast(this.mKeyguardViewMediator);
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
        ((KeyguardViewMediatorInjector) Dependency.get(KeyguardViewMediatorInjector.class)).preHideKeyguard();
    }

    private void wakeupIfNeed(String str) {
        if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive()) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), str);
        }
    }

    private boolean supportFastUnlock() {
        return MiuiKeyguardUtils.isGxzwSensor() || MiuiKeyguardUtils.isBroadSideFingerprint();
    }
}
