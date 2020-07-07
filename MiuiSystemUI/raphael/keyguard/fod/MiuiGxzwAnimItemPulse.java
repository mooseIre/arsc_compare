package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAnimItemPulse extends MiuiGxzwAnimItem {
    public int getFodMotionRtpId() {
        return 80;
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
