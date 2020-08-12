package com.android.systemui.biometrics;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.systemui.plugins.R;

public class FingerprintDialogView extends BiometricDialogView {
    /* access modifiers changed from: protected */
    public int getAuthenticatedAccessibilityResourceId() {
        return 17040066;
    }

    /* access modifiers changed from: protected */
    public int getDelayAfterAuthenticatedDurationMs() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getHintStringResourceId() {
        return R.string.fingerprint_dialog_touch_sensor;
    }

    /* access modifiers changed from: protected */
    public int getIconDescriptionResourceId() {
        return R.string.accessibility_fingerprint_dialog_fingerprint_icon;
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnimateForTransition(int i, int i2) {
        if (i == 0 && i2 == 1) {
            return false;
        }
        if (i == 1 && i2 == 2) {
            return true;
        }
        if (i == 2 && i2 == 1) {
            return true;
        }
        if (i != 1 || i2 == 4) {
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldGrayAreaDismissDialog() {
        return true;
    }

    public FingerprintDialogView(Context context, DialogViewCallback dialogViewCallback) {
        super(context, dialogViewCallback);
    }

    /* access modifiers changed from: protected */
    public void handleClearMessage(boolean z) {
        updateState(1);
        this.mErrorText.setText(getHintStringResourceId());
        this.mErrorText.setTextColor(this.mTextColor);
    }

    /* access modifiers changed from: protected */
    public Drawable getAnimationForTransition(int i, int i2) {
        int i3 = R.drawable.fingerprint_dialog_fp_to_error;
        if (!((i == 0 && i2 == 1) || (i == 1 && i2 == 2))) {
            if (i == 2 && i2 == 1) {
                i3 = R.drawable.fingerprint_dialog_error_to_fp;
            } else if (!(i == 1 && i2 == 4)) {
                return null;
            }
        }
        return this.mContext.getDrawable(i3);
    }
}
