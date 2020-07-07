package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.systemui.plugins.R;
import java.util.concurrent.TimeUnit;

public abstract class KeyguardAbsKeyInputView extends MiuiKeyguardPasswordView implements KeyguardSecurityView, EmergencyButton.EmergencyButtonCallback {
    private CountDownTimer mCountdownTimer;
    private boolean mDismissing;
    protected boolean mEnableHaptics;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    protected SecurityMessageDisplay mSecurityMessageDisplay;

    /* access modifiers changed from: protected */
    public abstract String getPasswordText();

    /* access modifiers changed from: protected */
    public abstract int getPasswordTextViewId();

    public boolean needsInput() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract void resetPasswordText(boolean z, boolean z2);

    /* access modifiers changed from: protected */
    public abstract void resetState();

    /* access modifiers changed from: protected */
    public abstract void setPasswordEntryEnabled(boolean z);

    /* access modifiers changed from: protected */
    public abstract void setPasswordEntryInputEnabled(boolean z);

    /* access modifiers changed from: protected */
    public boolean shouldLockout(long j) {
        return j != 0;
    }

    public KeyguardAbsKeyInputView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCountdownTimer = null;
    }

    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mEnableHaptics = lockPatternUtils.isTactileFeedbackEnabled();
    }

    public void reset() {
        this.mDismissing = false;
        resetPasswordText(false, false);
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        if (shouldLockout(lockoutAttemptDeadline)) {
            handleAttemptLockout(lockoutAttemptDeadline);
        } else {
            resetState();
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
    }

    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (!this.mDismissing) {
            String passwordText = getPasswordText();
            final int i = 0;
            setPasswordEntryInputEnabled(false);
            AsyncTask<?, ?, ?> asyncTask = this.mPendingLockCheck;
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (passwordText.length() <= 3) {
                setPasswordEntryInputEnabled(true);
                onPasswordChecked(currentUser, false, 0, false);
                return;
            }
            if (currentUser == 0) {
                i = KeyguardUpdateMonitor.getSecondUser();
            }
            if (LatencyTracker.isEnabled(this.mContext)) {
                LatencyTracker.getInstance(this.mContext).onActionStart(3);
                LatencyTracker.getInstance(this.mContext).onActionStart(4);
            }
            AnalyticsHelper.getInstance(this.mContext).trackPageStart("pw_unlock_time");
            final long currentTimeMillis = System.currentTimeMillis();
            this.mPendingLockCheck = LockPatternChecker.checkPasswordForUsers(this.mLockPatternUtils, passwordText, currentUser, i, this.mContext, new OnCheckForUsersCallback() {
                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "password unlock duration " + (System.currentTimeMillis() - currentTimeMillis));
                    KeyguardAbsKeyInputView.this.handleUserCheckMatched(currentUser);
                }

                public void onChecked(boolean z, int i, int i2) {
                    if (LatencyTracker.isEnabled(KeyguardAbsKeyInputView.this.mContext)) {
                        LatencyTracker.getInstance(KeyguardAbsKeyInputView.this.mContext).onActionEnd(4);
                    }
                    KeyguardAbsKeyInputView.this.setPasswordEntryInputEnabled(true);
                    KeyguardAbsKeyInputView keyguardAbsKeyInputView = KeyguardAbsKeyInputView.this;
                    keyguardAbsKeyInputView.mPendingLockCheck = null;
                    if (!z) {
                        keyguardAbsKeyInputView.onPasswordChecked(i, false, i2, true);
                    }
                }
            }, new OnCheckForUsersCallback() {
                public void onChecked(boolean z, int i, int i2) {
                }

                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "password unlock duration other user" + (System.currentTimeMillis() - currentTimeMillis));
                    KeyguardAbsKeyInputView.this.handleUserCheckMatched(i);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void handleUserCheckMatched(int i) {
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionEnd(3);
        }
        AnalyticsHelper.getInstance(this.mContext).trackPageEnd("pw_verify_time");
        onPasswordChecked(i, true, 0, true);
    }

    /* access modifiers changed from: private */
    public void onPasswordChecked(int i, boolean z, int i2, boolean z2) {
        if (!z) {
            if (z2) {
                this.mCallback.reportUnlockAttempt(i, false, i2);
                if (i2 > 0) {
                    FaceUnlockManager.getInstance().stopFaceUnlock();
                    handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            handleWrongPassword();
            this.mVibrator.vibrate(150);
            AnalyticsHelper.getInstance(this.mContext).recordUnlockWay("pw", false);
        } else if (!allowUnlock(i)) {
            resetPasswordText(true, false);
            return;
        } else {
            switchUser(i);
            this.mCallback.reportUnlockAttempt(i, true, 0);
            this.mDismissing = true;
            this.mCallback.dismiss(true, i);
            AnalyticsHelper.getInstance(this.mContext).recordUnlockWay("pw", true);
        }
        resetPasswordText(true, !z);
    }

    /* access modifiers changed from: protected */
    public void handleAttemptLockout(long j) {
        long elapsedRealtime = j - SystemClock.elapsedRealtime();
        this.mCallback.handleAttemptLockout(elapsedRealtime);
        setPasswordEntryEnabled(false);
        this.mCountdownTimer = new CountDownTimer(((long) Math.ceil(((double) elapsedRealtime) / 1000.0d)) * 1000, 1000) {
            public void onTick(long j) {
            }

            public void onFinish() {
                KeyguardAbsKeyInputView.this.resetState();
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public void onUserInput() {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.userActivity();
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        onUserInput();
        return false;
    }

    public void onPause() {
        CountDownTimer countDownTimer = this.mCountdownTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.mCountdownTimer = null;
        }
        AsyncTask<?, ?, ?> asyncTask = this.mPendingLockCheck;
        if (asyncTask != null) {
            asyncTask.cancel(false);
            this.mPendingLockCheck = null;
        }
    }

    public void onResume(int i) {
        reset();
    }

    public void showPromptReason(int i) {
        if (i != 0) {
            String promptReasonString = getPromptReasonString(i);
            if (!TextUtils.isEmpty(promptReasonString)) {
                this.mKeyguardBouncerMessageView.showMessage(this.mContext.getResources().getString(R.string.input_password_hint_text), promptReasonString);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getPromptReasonString(int i) {
        Resources resources = this.mContext.getResources();
        if (i == 0) {
            return "";
        }
        if (i == 1) {
            return resources.getString(R.string.input_password_after_boot_msg);
        }
        if (i == 2) {
            long requiredStrongAuthTimeout = getRequiredStrongAuthTimeout();
            return resources.getQuantityString(R.plurals.input_password_after_timeout_msg, (int) TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout), new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout))});
        } else if (i == 3) {
            return resources.getString(R.string.kg_prompt_reason_device_admin);
        } else {
            if (i != 4) {
                return resources.getString(R.string.kg_prompt_reason_timeout_password);
            }
            return resources.getString(R.string.kg_prompt_reason_user_request);
        }
    }

    public void showMessage(String str, String str2, int i) {
        this.mKeyguardBouncerMessageView.showMessage(str, str2, i);
    }

    public void applyHintAnimation(long j) {
        this.mKeyguardBouncerMessageView.applyHintAnimation(j);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    public boolean startDisappearAnimation(Runnable runnable) {
        this.mFaceUnlockView.setVisibility(4);
        return false;
    }
}
