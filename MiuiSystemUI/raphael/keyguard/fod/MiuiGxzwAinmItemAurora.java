package com.android.keyguard.fod;

public class MiuiGxzwAinmItemAurora extends MiuiGxzwAnimItem {
    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public int getFodMotionRtpId() {
        return 160;
    }

    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(27, "gxzw_aurora_recognizing_anim_", true, 30, false);
    }
}
