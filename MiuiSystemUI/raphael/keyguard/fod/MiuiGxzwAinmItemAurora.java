package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAinmItemAurora extends MiuiGxzwAnimItem {
    public int getFodMotionRtpId() {
        return 81;
    }

    public /* bridge */ /* synthetic */ int getFalseTipTranslationY(Context context) {
        return super.getFalseTipTranslationY(context);
    }

    public /* bridge */ /* synthetic */ boolean isDismissRecognizingWhenFalse() {
        return super.isDismissRecognizingWhenFalse();
    }

    public /* bridge */ /* synthetic */ boolean isShowIconWhenRecognizingStart() {
        return super.isShowIconWhenRecognizingStart();
    }

    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(41, "gxzw_aurora_recognizing_anim_", true, 17, false);
    }

    public static boolean supportAuroraAnim(Context context) {
        return context.getResources().getIdentifier("gxzw_aurora_recognizing_anim_1", "drawable", context.getPackageName()) != 0;
    }
}
