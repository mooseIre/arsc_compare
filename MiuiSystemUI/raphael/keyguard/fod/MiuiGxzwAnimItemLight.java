package com.android.keyguard.fod;

public class MiuiGxzwAnimItemLight extends MiuiGxzwAnimItem {
    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public int getFodMotionRtpId() {
        return 159;
    }

    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(40, "gxzw_light_recognizing_anim_", true, 17, false);
    }
}
