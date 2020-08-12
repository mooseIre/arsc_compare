package com.android.systemui.recents.misc;

import android.os.Handler;
import android.view.ViewDebug;

public class DozeTrigger {
    @ViewDebug.ExportedProperty(category = "recents")
    int mDozeDurationMilliseconds;
    Runnable mDozeRunnable = new Runnable() {
        public void run() {
            DozeTrigger dozeTrigger = DozeTrigger.this;
            dozeTrigger.mIsDozing = false;
            dozeTrigger.mIsAsleep = true;
            dozeTrigger.mOnSleepRunnable.run();
        }
    };
    Handler mHandler = new Handler();
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsAsleep;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsDozing;
    Runnable mOnSleepRunnable;

    public DozeTrigger(int i, Runnable runnable) {
        this.mDozeDurationMilliseconds = i;
        this.mOnSleepRunnable = runnable;
    }

    public void startDozing() {
        forcePoke();
        this.mIsAsleep = false;
    }

    public void stopDozing() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mIsDozing = false;
        this.mIsAsleep = false;
    }

    public void setDozeDuration(int i) {
        this.mDozeDurationMilliseconds = i;
    }

    public void poke() {
        if (this.mIsDozing) {
            forcePoke();
        }
    }

    /* access modifiers changed from: package-private */
    public void forcePoke() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mHandler.postDelayed(this.mDozeRunnable, (long) this.mDozeDurationMilliseconds);
        this.mIsDozing = true;
    }

    public boolean isDozing() {
        return this.mIsDozing;
    }

    public boolean isAsleep() {
        return this.mIsAsleep;
    }
}
