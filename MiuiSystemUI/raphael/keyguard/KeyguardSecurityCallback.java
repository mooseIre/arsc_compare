package com.android.keyguard;

public interface KeyguardSecurityCallback {
    void dismiss(boolean z, int i);

    void dismiss(boolean z, int i, boolean z2);

    void handleAttemptLockout(long j);

    void onUserInput();

    void reportUnlockAttempt(int i, boolean z, int i2);

    void reset();

    void userActivity();
}
