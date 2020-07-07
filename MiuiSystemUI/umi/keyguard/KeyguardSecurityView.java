package com.android.keyguard;

import com.android.internal.widget.LockPatternUtils;

public interface KeyguardSecurityView {
    void applyHintAnimation(long j);

    boolean needsInput();

    void onPause();

    void onResume(int i);

    void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback);

    void setLockPatternUtils(LockPatternUtils lockPatternUtils);

    void showMessage(String str, String str2, int i);

    void showPromptReason(int i);

    void startAppearAnimation();

    boolean startDisappearAnimation(Runnable runnable);
}
