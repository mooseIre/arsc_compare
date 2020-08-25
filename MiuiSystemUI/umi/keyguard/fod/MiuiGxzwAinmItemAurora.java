package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAinmItemAurora extends MiuiGxzwAnimItem {
    public int getFodMotionRtpId() {
        return 81;
    }

    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(41, "gxzw_aurora_recognizing_anim_", true, 17, false);
    }

    public static boolean supportAuroraAnim(Context context) {
        return context.getResources().getIdentifier("gxzw_aurora_recognizing_anim_1", "drawable", context.getPackageName()) != 0;
    }
}
