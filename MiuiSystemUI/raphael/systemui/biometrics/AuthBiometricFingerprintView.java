package com.android.systemui.biometrics;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;

public class AuthBiometricFingerprintView extends AuthBiometricView {
    private boolean shouldAnimateForTransition(int i, int i2) {
        return (i2 == 1 || i2 == 2) ? i == 4 || i == 3 : i2 == 3 || i2 == 4;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getDelayAfterAuthenticatedDurationMs() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getStateForAfterError() {
        return 2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public boolean supportsSmallDialog() {
        return false;
    }

    public AuthBiometricFingerprintView(Context context) {
        this(context, null);
    }

    public AuthBiometricFingerprintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterError() {
        showTouchSensorString();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterHelp() {
        showTouchSensorString();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void updateState(int i) {
        updateIcon(this.mState, i);
        super.updateState(i);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onAttachedToWindowInternal() {
        super.onAttachedToWindowInternal();
        showTouchSensorString();
    }

    private void showTouchSensorString() {
        this.mIndicatorView.setText(C0021R$string.fingerprint_dialog_touch_sensor);
        this.mIndicatorView.setTextColor(C0011R$color.biometric_dialog_gray);
    }

    private void updateIcon(int i, int i2) {
        if (!MiuiKeyguardUtils.isGxzwSensor()) {
            Drawable animationForTransition = getAnimationForTransition(i, i2);
            if (animationForTransition == null) {
                Log.e("BiometricPrompt/AuthBiometricFingerprintView", "Animation not found, " + i + " -> " + i2);
                return;
            }
            AnimatedVectorDrawable animatedVectorDrawable = animationForTransition instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) animationForTransition : null;
            this.mIconView.setImageDrawable(animationForTransition);
            if (animatedVectorDrawable != null && shouldAnimateForTransition(i, i2)) {
                animatedVectorDrawable.forceAnimationOnUI();
                animatedVectorDrawable.start();
            }
        }
    }

    private Drawable getAnimationForTransition(int i, int i2) {
        int i3;
        if (i2 == 1 || i2 == 2) {
            if (i == 4 || i == 3) {
                i3 = C0013R$drawable.fingerprint_dialog_error_to_fp;
            } else {
                i3 = C0013R$drawable.fingerprint_dialog_fp_to_error;
            }
        } else if (i2 == 3 || i2 == 4) {
            i3 = C0013R$drawable.fingerprint_dialog_fp_to_error;
        } else if (i2 != 6) {
            return null;
        } else {
            i3 = C0013R$drawable.fingerprint_dialog_fp_to_error;
        }
        return ((LinearLayout) this).mContext.getDrawable(i3);
    }
}
