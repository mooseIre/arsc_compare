package com.android.keyguard.fod;

public class MiuiGxzwAnimItemPulse extends MiuiGxzwAnimItem {
    public int getFodMotionRtpId() {
        return 80;
    }

    public MiuiGxzwAnimRes generalNormalRecognizing() {
        if (MiuiGxzwUtils.isSpecialCepheus()) {
            return new MiuiGxzwAnimRes(25, "gxzw_pulse_recognizing_anim_white_", true, 30, false);
        }
        return new MiuiGxzwAnimRes(25, "gxzw_pulse_recognizing_anim_", true, 30, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightRecognizing() {
        return new MiuiGxzwAnimRes(25, "gxzw_pulse_recognizing_anim_", true, 30, false);
    }
}
