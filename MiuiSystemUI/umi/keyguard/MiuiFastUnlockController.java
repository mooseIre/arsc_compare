package com.android.keyguard;

import android.content.Context;
import android.os.PowerManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MiuiFastUnlockController {
    private final ArrayList<WeakReference<FastUnlockCallback>> mCallbacks = new ArrayList<>();
    private Context mContext;
    private volatile boolean mFastUnlock = false;

    public interface FastUnlockCallback {
    }

    public MiuiFastUnlockController(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
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
}
