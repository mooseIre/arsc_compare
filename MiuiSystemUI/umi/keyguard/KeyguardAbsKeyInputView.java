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
import android.view.MotionEvent;
import android.widget.LinearLayout;
import codeinjection.CodeInjection;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0018R$plurals;
import com.android.systemui.C0020R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.concurrent.TimeUnit;

public abstract class KeyguardAbsKeyInputView extends MiuiKeyguardPasswordView implements KeyguardSecurityView, EmergencyButton.EmergencyButtonCallback {
    private CountDownTimer mCountdownTimer;
    private boolean mDismissing;
    protected boolean mEnableHaptics;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    protected boolean mResumed;
    protected SecurityMessageDisplay mSecurityMessageDisplay;
    private KeyguardSecurityModel mSecurityModel;

    /* access modifiers changed from: protected */
    public abstract LockscreenCredential getEnteredCredential();

    /* access modifiers changed from: protected */
    public abstract int getPasswordTextViewId();

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
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
        this(context, null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCountdownTimer = null;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mEnableHaptics = lockPatternUtils.isTactileFeedbackEnabled();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
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
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(((LinearLayout) this).mContext);
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mSecurityModel = (KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback, com.android.keyguard.MiuiKeyguardPasswordView
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (!this.mDismissing) {
            LockscreenCredential enteredCredential = getEnteredCredential();
            final int i = 0;
            setPasswordEntryInputEnabled(false);
            AsyncTask<?, ?, ?> asyncTask = this.mPendingLockCheck;
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (enteredCredential.size() <= 3) {
                setPasswordEntryInputEnabled(true);
                onPasswordChecked(currentUser, false, 0, false);
                return;
            }
            if (currentUser == 0) {
                UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
                i = UserSwitcherController.getSecondUser();
            }
            if (LatencyTracker.isEnabled(((LinearLayout) this).mContext)) {
                LatencyTracker.getInstance(((LinearLayout) this).mContext).onActionStart(3);
                LatencyTracker.getInstance(((LinearLayout) this).mContext).onActionStart(4);
            }
            final long currentTimeMillis = System.currentTimeMillis();
            this.mPendingLockCheck = MiuiLockPatternChecker.checkCredentialForUsers(this.mLockPatternUtils, enteredCredential, currentUser, i, ((LinearLayout) this).mContext, new OnCheckForUsersCallback() {
                /* class com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass1 */

                @Override // com.android.keyguard.OnCheckForUsersCallback
                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "password unlock duration " + (System.currentTimeMillis() - currentTimeMillis));
                    KeyguardAbsKeyInputView.this.handleUserCheckMatched(currentUser);
                }

                @Override // com.android.keyguard.OnCheckForUsersCallback
                public void onChecked(boolean z, int i, int i2) {
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardAbsKeyInputView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).onActionEnd(4);
                    }
                    KeyguardAbsKeyInputView.this.setPasswordEntryInputEnabled(true);
                    KeyguardAbsKeyInputView keyguardAbsKeyInputView = KeyguardAbsKeyInputView.this;
                    keyguardAbsKeyInputView.mPendingLockCheck = null;
                    if (!z) {
                        keyguardAbsKeyInputView.onPasswordChecked(i, false, i2, true);
                    }
                }
            }, new OnCheckForUsersCallback() {
                /* class com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass2 */

                @Override // com.android.keyguard.OnCheckForUsersCallback
                public void onChecked(boolean z, int i, int i2) {
                }

                @Override // com.android.keyguard.OnCheckForUsersCallback
                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "password unlock duration other user" + (System.currentTimeMillis() - currentTimeMillis));
                    KeyguardAbsKeyInputView.this.handleUserCheckMatched(i);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserCheckMatched(int i) {
        if (LatencyTracker.isEnabled(((LinearLayout) this).mContext)) {
            LatencyTracker.getInstance(((LinearLayout) this).mContext).onActionEnd(3);
        }
        onPasswordChecked(i, true, 0, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPasswordChecked(int i, boolean z, int i2, boolean z2) {
        if (!z) {
            if (z2) {
                this.mCallback.reportUnlockAttempt(i, false, i2);
                if (i2 > 0) {
                    this.mKeyguardUpdateMonitor.cancelFaceAuth();
                    handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            handleWrongPassword();
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(76, true, 150);
        } else if (!allowUnlock(i)) {
            resetPasswordText(true, false);
            return;
        } else {
            switchUser(i);
            this.mCallback.reportUnlockAttempt(i, true, 0);
            this.mDismissing = true;
            this.mCallback.dismiss(true, i);
        }
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("pw", z);
        resetPasswordText(true, !z);
    }

    /* access modifiers changed from: protected */
    public void handleAttemptLockout(long j) {
        long elapsedRealtime = j - SystemClock.elapsedRealtime();
        this.mCallback.handleAttemptLockout(elapsedRealtime);
        setPasswordEntryEnabled(false);
        this.mCountdownTimer = new CountDownTimer(((long) Math.ceil(((double) elapsedRealtime) / 1000.0d)) * 1000, 1000) {
            /* class com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass3 */

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
            this.mCallback.onUserInput();
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 0) {
            return false;
        }
        onUserInput();
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        this.mResumed = false;
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
        reset();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        this.mResumed = true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        if (i != 0) {
            String promptReasonString = getPromptReasonString(i);
            if (!TextUtils.isEmpty(promptReasonString)) {
                this.mKeyguardBouncerMessageView.showMessage(getPromptTitle(), promptReasonString);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.keyguard.KeyguardAbsKeyInputView$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.android.keyguard.KeyguardSecurityModel$SecurityMode[] r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass4.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = r0
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.PIN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass4.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Password     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardAbsKeyInputView.AnonymousClass4.<clinit>():void");
        }
    }

    private String getPromptTitle() {
        int i = AnonymousClass4.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser()).ordinal()];
        if (i == 1) {
            return ((LinearLayout) this).mContext.getResources().getString(C0020R$string.input_lockscreen_pin_hint_text);
        }
        if (i != 2) {
            return ((LinearLayout) this).mContext.getResources().getString(C0020R$string.input_password_hint_text);
        }
        return ((LinearLayout) this).mContext.getResources().getString(C0020R$string.input_lockscreen_password_hint_text);
    }

    /* access modifiers changed from: protected */
    public String getPromptReasonString(int i) {
        Resources resources = ((LinearLayout) this).mContext.getResources();
        if (i == 0) {
            return CodeInjection.MD5;
        }
        if (i == 1) {
            return resources.getString(C0020R$string.input_password_after_boot_msg);
        }
        if (i == 2) {
            long requiredStrongAuthTimeout = getRequiredStrongAuthTimeout();
            return resources.getQuantityString(C0018R$plurals.input_password_after_timeout_msg, (int) TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout), Long.valueOf(TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout)));
        } else if (i == 3) {
            return resources.getString(C0020R$string.kg_prompt_reason_device_admin);
        } else {
            if (i != 4) {
                return resources.getString(C0020R$string.kg_prompt_reason_timeout_password);
            }
            return resources.getString(C0020R$string.kg_prompt_reason_user_request);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, String str2, int i) {
        this.mKeyguardBouncerMessageView.showMessage(str, str2, i);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void applyHintAnimation(long j) {
        this.mKeyguardBouncerMessageView.applyHintAnimation(j);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        this.mFaceUnlockView.setVisibility(4);
        return false;
    }
}
