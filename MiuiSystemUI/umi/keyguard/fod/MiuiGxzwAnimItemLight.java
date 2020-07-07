package com.android.keyguard.fod;

import android.content.Context;
import com.android.systemui.plugins.R;

public class MiuiGxzwAnimItemLight extends MiuiGxzwAnimItem {
    public boolean isDismissRecognizingWhenFalse() {
        return false;
    }

    public int getFalseTipTranslationY(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.gxzw_light_false_tip_translation_y);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodRecognizing() {
        return new MiuiGxzwAnimRes(29, "gxzw_aod_recognizing_anim_", false, 30, true);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodFalse() {
        return new MiuiGxzwAnimRes(15, "gxzw_aod_false_anim_", false, 30, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodBack() {
        return new MiuiGxzwAnimRes(15, "gxzw_aod_back_anim_", false, 30, false);
    }

    public MiuiGxzwAnimRes generalNormalRecognizing() {
        return new MiuiGxzwAnimRes(29, "gxzw_normal_recognizing_anim_", false, 30, true);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalFalse() {
        return new MiuiGxzwAnimRes(15, "gxzw_normal_false_anim_", false, 30, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalBack() {
        return new MiuiGxzwAnimRes(15, "gxzw_normal_back_anim_", false, 30, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightRecognizing() {
        return new MiuiGxzwAnimRes(29, "gxzw_light_recognizing_anim_", false, 30, true);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightFalse() {
        return new MiuiGxzwAnimRes(15, "gxzw_light_false_anim_", false, 30, false);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightBack() {
        return new MiuiGxzwAnimRes(15, "gxzw_light_back_anim_", false, 30, false);
    }
}
