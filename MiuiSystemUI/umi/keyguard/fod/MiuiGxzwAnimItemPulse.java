package com.android.keyguard.fod;

public class MiuiGxzwAnimItemPulse extends MiuiGxzwAnimItem {
    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public int getFodMotionRtpId() {
        return 157;
    }

    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(27, "gxzw_pulse_recognizing_anim_", true, 30, false);
    }
}
