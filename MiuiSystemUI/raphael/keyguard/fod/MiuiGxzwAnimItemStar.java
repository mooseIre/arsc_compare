package com.android.keyguard.fod;

public class MiuiGxzwAnimItemStar extends MiuiGxzwAnimItem {
    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public int getFodMotionRtpId() {
        return 158;
    }

    @Override // com.android.keyguard.fod.MiuiGxzwAnimItem
    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(27, "gxzw_star_recognizing_anim_", true, 30, false);
    }
}
