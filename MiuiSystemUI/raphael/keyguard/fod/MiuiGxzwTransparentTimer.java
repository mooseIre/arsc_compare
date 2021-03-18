package com.android.keyguard.fod;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

/* access modifiers changed from: package-private */
public class MiuiGxzwTransparentTimer implements AlarmManager.OnAlarmListener {
    private final AlarmManager mAlarmManager;
    private Handler mHandler = new Handler();
    private boolean mSetAlarm = false;
    private Runnable mTimeout = new Runnable() {
        /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwTransparentTimer$Pk9iG6cYSfTC_ZpI6uzaoeYdtTU */

        public final void run() {
            MiuiGxzwTransparentTimer.this.lambda$new$0$MiuiGxzwTransparentTimer();
        }
    };
    private TransparentTimerListener mTransparentTimerListener;

    /* access modifiers changed from: package-private */
    public interface TransparentTimerListener {
        void onTransparentTimeout();
    }

    public void onPause() {
    }

    public void onResume() {
    }

    MiuiGxzwTransparentTimer(Context context) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
    }

    public void schedule(TransparentTimerListener transparentTimerListener) {
        cancel();
        this.mTransparentTimerListener = transparentTimerListener;
        this.mHandler.postDelayed(this.mTimeout, 3000);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 3000, "MiuiGxzwTransparentTimer", this, this.mHandler);
        this.mSetAlarm = true;
    }

    public void cancel() {
        this.mTransparentTimerListener = null;
        this.mHandler.removeCallbacks(this.mTimeout);
        if (this.mSetAlarm) {
            this.mAlarmManager.cancel(this);
        }
        this.mSetAlarm = false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiGxzwTransparentTimer() {
        TransparentTimerListener transparentTimerListener = this.mTransparentTimerListener;
        if (transparentTimerListener != null) {
            transparentTimerListener.onTransparentTimeout();
            cancel();
        }
    }

    public void onAlarm() {
        TransparentTimerListener transparentTimerListener = this.mTransparentTimerListener;
        if (transparentTimerListener != null) {
            transparentTimerListener.onTransparentTimeout();
            cancel();
        }
    }
}
