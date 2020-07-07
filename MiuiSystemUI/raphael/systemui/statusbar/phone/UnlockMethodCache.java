package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Trace;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import java.util.ArrayList;
import java.util.Iterator;

public class UnlockMethodCache {
    private static UnlockMethodCache sInstance;
    private final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int i) {
            UnlockMethodCache.this.update(false);
            UnlockMethodCache.this.updateSecure();
        }

        public void onStartedWakingUp() {
            UnlockMethodCache.this.update(false);
        }

        public void onFingerprintAuthenticated(int i) {
            Trace.beginSection("KeyguardUpdateMonitorCallback#onFingerprintAuthenticated");
            if (!UnlockMethodCache.this.mKeyguardUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                Trace.endSection();
                return;
            }
            UnlockMethodCache.this.update(false);
            Trace.endSection();
        }

        public void onFaceUnlockStateChanged(boolean z, int i) {
            UnlockMethodCache.this.update(false);
        }

        public void onStrongAuthStateChanged(int i) {
            UnlockMethodCache.this.update(false);
        }

        public void onScreenTurnedOff() {
            UnlockMethodCache.this.update(false);
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            UnlockMethodCache.this.update(false);
        }

        public void onDevicePolicyManagerStateChanged() {
            UnlockMethodCache.this.mHandler.removeCallbacks(UnlockMethodCache.this.mUpdateSecureRunnable);
            UnlockMethodCache.this.mHandler.postDelayed(UnlockMethodCache.this.mUpdateSecureRunnable, 50);
        }
    };
    private boolean mCanSkipBouncer;
    private final FaceUnlockCallback mFaceUnlockCallback = new FaceUnlockCallback() {
        public void onFaceAuthenticated() {
            Trace.beginSection("KeyguardUpdateMonitorCallback#onFaceAuthenticated");
            if (!UnlockMethodCache.this.mKeyguardUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                Trace.endSection();
                return;
            }
            UnlockMethodCache.this.update(false);
            Trace.endSection();
        }
    };
    private boolean mFaceUnlockRunning;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ArrayList<OnUnlockMethodChangedListener> mListeners = new ArrayList<>();
    private final LockPatternUtils mLockPatternUtils;
    private boolean mSecure;
    private boolean mTrustManaged;
    private boolean mTrusted;
    /* access modifiers changed from: private */
    public final Runnable mUpdateSecureRunnable = new Runnable() {
        public void run() {
            UnlockMethodCache.this.updateSecure();
        }
    };

    public interface OnUnlockMethodChangedListener {
        void onUnlockMethodStateChanged();
    }

    private UnlockMethodCache(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
        FaceUnlockManager.getInstance().registerFaceUnlockCallback(this.mFaceUnlockCallback);
        update(true);
        updateSecure();
    }

    public static UnlockMethodCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UnlockMethodCache(context);
        }
        return sInstance;
    }

    public boolean isMethodSecure(int i) {
        if (i != KeyguardUpdateMonitor.getCurrentUser()) {
            return this.mLockPatternUtils.isSecure(i);
        }
        return this.mSecure;
    }

    public boolean isMethodSecure() {
        return this.mSecure;
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public void addListener(OnUnlockMethodChangedListener onUnlockMethodChangedListener) {
        this.mListeners.add(onUnlockMethodChangedListener);
    }

    /* access modifiers changed from: private */
    public void update(boolean z) {
        Trace.beginSection("UnlockMethodCache#update");
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        boolean z2 = false;
        boolean z3 = !this.mSecure || this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(currentUser);
        boolean userTrustIsManaged = this.mKeyguardUpdateMonitor.getUserTrustIsManaged(currentUser);
        boolean userHasTrust = this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser);
        boolean z4 = this.mKeyguardUpdateMonitor.isFaceUnlockRunning(currentUser) && userTrustIsManaged;
        if (!(z3 == this.mCanSkipBouncer && userTrustIsManaged == this.mTrustManaged && z4 == this.mFaceUnlockRunning)) {
            z2 = true;
        }
        if (z2 || z) {
            this.mCanSkipBouncer = z3;
            this.mTrusted = userHasTrust;
            this.mTrustManaged = userTrustIsManaged;
            this.mFaceUnlockRunning = z4;
            notifyListeners();
        }
        Trace.endSection();
    }

    public void updateSecure() {
        Trace.beginSection("UnlockMethodCache#updateSecure");
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        boolean isSecure = this.mLockPatternUtils.isSecure(currentUser);
        boolean z = false;
        boolean z2 = !isSecure || this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(currentUser);
        if (!(isSecure == this.mSecure && z2 == this.mCanSkipBouncer)) {
            z = true;
        }
        if (z) {
            this.mSecure = isSecure;
            this.mCanSkipBouncer = z2;
            notifyListeners();
        }
        Trace.endSection();
    }

    private void notifyListeners() {
        Iterator<OnUnlockMethodChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onUnlockMethodStateChanged();
        }
    }
}
