package com.android.systemui;

import android.content.Context;
import android.os.Vibrator;
import com.miui.systemui.annotation.Inject;
import miui.util.HapticFeedbackUtil;

public class HapticFeedBackImpl {
    HapticFeedbackUtil mHapticFeedbackUtil;
    protected Vibrator mVibrator;

    public HapticFeedBackImpl(@Inject Context context) {
        this.mHapticFeedbackUtil = new HapticFeedbackUtil(context, false);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
    }

    public HapticFeedbackUtil getHapticFeedbackUtil() {
        return this.mHapticFeedbackUtil;
    }

    public void clearNotification() {
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            try {
                this.mHapticFeedbackUtil.performExtHapticFeedback(92);
            } catch (Exception unused) {
            }
        }
    }

    public void clearAllNotifications() {
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            try {
                this.mHapticFeedbackUtil.performExtHapticFeedback(93);
            } catch (Exception unused) {
            }
        }
    }

    public void meshNormal() {
        hapticFeedback("mesh_normal", false);
    }

    public void flick() {
        hapticFeedback("flick", false);
    }

    public void hapticFeedback(String str, boolean z) {
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            this.mHapticFeedbackUtil.performHapticFeedback(str, z);
        }
    }

    public void extHapticFeedback(int i, boolean z, int i2) {
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            this.mHapticFeedbackUtil.performExtHapticFeedback(i);
        } else if (z) {
            this.mVibrator.vibrate((long) i2);
        }
    }
}
