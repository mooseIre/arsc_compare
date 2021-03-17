package com.android.systemui.biometrics;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockscreenCredential;
import com.android.systemui.C0015R$id;
import com.android.systemui.biometrics.AuthCredentialPatternView;
import java.util.List;

public class AuthCredentialPatternView extends AuthCredentialView {
    private LockPatternView mLockPatternView;

    /* access modifiers changed from: private */
    public class UnlockPatternListener implements LockPatternView.OnPatternListener {
        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
        }

        public void onPatternCleared() {
        }

        public void onPatternStart() {
        }

        private UnlockPatternListener() {
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            AsyncTask<?, ?, ?> asyncTask = AuthCredentialPatternView.this.mPendingLockCheck;
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
            AuthCredentialPatternView.this.mLockPatternView.setEnabled(false);
            if (list.size() < 4) {
                onPatternVerified(null, 0);
                return;
            }
            LockscreenCredential createPattern = LockscreenCredential.createPattern(list);
            try {
                AuthCredentialPatternView.this.mPendingLockCheck = LockPatternChecker.verifyCredential(AuthCredentialPatternView.this.mLockPatternUtils, createPattern, AuthCredentialPatternView.this.mOperationId, AuthCredentialPatternView.this.mEffectiveUserId, new LockPatternChecker.OnVerifyCallback() {
                    /* class com.android.systemui.biometrics.$$Lambda$AuthCredentialPatternView$UnlockPatternListener$i26rXOj6tOr6sIKp7_roy07Tuw */

                    public final void onVerified(byte[] bArr, int i) {
                        AuthCredentialPatternView.UnlockPatternListener.m11lambda$i26rXOj6tOr6sIKp7_roy07Tuw(AuthCredentialPatternView.UnlockPatternListener.this, bArr, i);
                    }
                });
                if (createPattern != null) {
                    createPattern.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            throw th;
        }

        /* access modifiers changed from: private */
        public void onPatternVerified(byte[] bArr, int i) {
            AuthCredentialPatternView.this.onCredentialVerified(bArr, i);
            if (i > 0) {
                AuthCredentialPatternView.this.mLockPatternView.setEnabled(false);
            } else {
                AuthCredentialPatternView.this.mLockPatternView.setEnabled(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthCredentialView
    public void onErrorTimeoutFinish() {
        super.onErrorTimeoutFinish();
        this.mLockPatternView.setEnabled(true);
    }

    public AuthCredentialPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthCredentialView
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        LockPatternView findViewById = findViewById(C0015R$id.lockPattern);
        this.mLockPatternView = findViewById;
        findViewById.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(this.mUserId));
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
    }
}
