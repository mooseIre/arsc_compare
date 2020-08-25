package com.android.systemui.biometrics;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.plugins.R;

public class AuthBiometricFingerprintView extends AuthBiometricView {
    /* access modifiers changed from: protected */
    public int getDelayAfterAuthenticatedDurationMs() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getStateForAfterError() {
        return 2;
    }

    /* access modifiers changed from: protected */
    public boolean supportsSmallDialog() {
        return false;
    }

    public AuthBiometricFingerprintView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AuthBiometricFingerprintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void handleResetAfterError() {
        showTouchSensorString();
    }

    /* access modifiers changed from: protected */
    public void handleResetAfterHelp() {
        showTouchSensorString();
    }

    public void updateState(int i) {
        super.updateState(i);
    }

    /* access modifiers changed from: package-private */
    public void onAttachedToWindowInternal() {
        super.onAttachedToWindowInternal();
        showTouchSensorString();
    }

    private void showTouchSensorString() {
        this.mIndicatorView.setText(R.string.fingerprint_dialog_touch_sensor);
        this.mIndicatorView.setTextColor(R.color.biometric_dialog_gray);
    }
}
