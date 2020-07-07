package com.android.systemui;

import android.content.Context;
import com.miui.systemui.annotation.Inject;
import miui.util.HapticFeedbackUtil;

public class HapticFeedBackImpl {
    HapticFeedbackUtil mHapticFeedbackUtil;

    public HapticFeedBackImpl(@Inject Context context) {
        this.mHapticFeedbackUtil = new HapticFeedbackUtil(context, false);
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
}
