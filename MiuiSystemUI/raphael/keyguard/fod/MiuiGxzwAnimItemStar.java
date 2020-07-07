package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAnimItemStar extends MiuiGxzwAnimItem {
    public int getFodMotionRtpId() {
        return 77;
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
        return new MiuiGxzwAnimRes(14, "gxzw_pop_recognizing_anim_", true, 30, false);
    }
}
