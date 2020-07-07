package com.android.keyguard.fod;

import android.content.Context;

public class MiuiGxzwAnimItemCircle extends MiuiGxzwAnimItem {
    public boolean isDismissRecognizingWhenFalse() {
        return false;
    }

    public boolean isShowIconWhenRecognizingStart() {
        return false;
    }

    public /* bridge */ /* synthetic */ int getFalseTipTranslationY(Context context) {
        return super.getFalseTipTranslationY(context);
    }

    public /* bridge */ /* synthetic */ int getFodMotionRtpId() {
        return super.getFodMotionRtpId();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodIcon() {
        return generalNormalIcon();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodRecognizing() {
        return generalNormalRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodFalse() {
        return generalNormalFalse();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodBack() {
        return generalNormalBack();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalIcon() {
        return new MiuiGxzwAnimRes(120, "gxzw_scan_anim_", true, 17, false);
    }

    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(30, "gxzw_circle_recognizing_anim_", true, 17, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalFalse() {
        return new MiuiGxzwAnimRes(30, "gxzw_circle_false_anim_", false, 17, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalBack() {
        return generalNormalIcon();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightIcon() {
        return generalNormalIcon();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightRecognizing() {
        return generalNormalRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightFalse() {
        return generalNormalFalse();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightBack() {
        return generalNormalBack();
    }
}
