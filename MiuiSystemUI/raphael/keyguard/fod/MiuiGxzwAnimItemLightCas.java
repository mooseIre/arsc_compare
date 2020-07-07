package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAnimItemLightCas extends MiuiGxzwAnimItem {
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
        return new MiuiGxzwAnimRes(40, "gxzw_light_cas_recognizing_anim_", true, 17, false);
    }
}
