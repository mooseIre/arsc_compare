package com.android.keyguard.fod;

import android.content.Context;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;

/* access modifiers changed from: package-private */
public abstract class MiuiGxzwAnimItem {
    private static final MiuiGxzwAnimRes DEFALUT_AOD_BACK = new MiuiGxzwAnimRes(new int[]{C0013R$drawable.finger_image_aod}, false, 30);
    private static final MiuiGxzwAnimRes DEFAULT_AOD_ICON = new MiuiGxzwAnimRes(35, "gxzw_aod_icon_anim_", false, 30, false);
    private static final MiuiGxzwAnimRes DEFAULT_LIGHT_BACK = new MiuiGxzwAnimRes(new int[]{C0013R$drawable.finger_image_light}, false, 30);
    private static final MiuiGxzwAnimRes DEFAULT_LIGHT_ICON = new MiuiGxzwAnimRes(35, "gxzw_light_icon_anim_", false, 30, false);
    private static final MiuiGxzwAnimRes DEFAULT_NORMAL_BACK = new MiuiGxzwAnimRes(new int[]{C0013R$drawable.finger_image_normal}, false, 30);
    private static final MiuiGxzwAnimRes DEFAULT_NORMAL_ICON = new MiuiGxzwAnimRes(35, "gxzw_normal_icon_anim_", false, 30, false);
    private final MiuiGxzwAnimRes mAodBack = generalAodBack();
    private final MiuiGxzwAnimRes mAodFalse = generalAodFalse();
    private final MiuiGxzwAnimRes mAodIcon = generalAodIcon();
    private final MiuiGxzwAnimRes mAodRecognizing = generalAodRecognizing();
    private final MiuiGxzwAnimRes mLightBack = generalLightBack();
    private final MiuiGxzwAnimRes mLightFalse = generalLightFalse();
    private final MiuiGxzwAnimRes mLightIcon = generalLightIcon();
    private final MiuiGxzwAnimRes mLightRecognizing = generalLightRecognizing();
    private final MiuiGxzwAnimRes mNormalBack = generalNormalBack();
    private final MiuiGxzwAnimRes mNormalFalse = generalNormalFalse();
    private final MiuiGxzwAnimRes mNormalIcon = generalNormalIcon();
    private final MiuiGxzwAnimRes mNormalRecognizing = generalNormalRecognizing();

    public abstract MiuiGxzwAnimRes generalNormalRecognizing();

    public int getFodMotionRtpId() {
        return 77;
    }

    public boolean isDismissRecognizingWhenFalse() {
        return true;
    }

    public boolean isShowIconWhenRecognizingStart() {
        return true;
    }

    MiuiGxzwAnimItem() {
    }

    public final MiuiGxzwAnimRes getIconAnimRes(boolean z, boolean z2) {
        if (z) {
            return this.mAodIcon;
        }
        if (z2) {
            return this.mLightIcon;
        }
        return this.mNormalIcon;
    }

    public final MiuiGxzwAnimRes getRecognizingAnimRes(boolean z, boolean z2) {
        if (z) {
            return this.mAodRecognizing;
        }
        if (z2) {
            return this.mLightRecognizing;
        }
        return this.mNormalRecognizing;
    }

    public final MiuiGxzwAnimRes getFalseAnimRes(boolean z, boolean z2) {
        if (z) {
            return this.mAodFalse;
        }
        if (z2) {
            return this.mLightFalse;
        }
        return this.mNormalFalse;
    }

    public final MiuiGxzwAnimRes getBackAnimRes(boolean z, boolean z2) {
        if (z) {
            return this.mAodBack;
        }
        if (z2) {
            return this.mLightBack;
        }
        return this.mNormalBack;
    }

    public int getFalseTipTranslationY(Context context) {
        return context.getResources().getDimensionPixelOffset(C0012R$dimen.gxzw_normal_false_tip_translation_y);
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodIcon() {
        return DEFAULT_AOD_ICON;
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodRecognizing() {
        return generalNormalRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodFalse() {
        return generalAodRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalAodBack() {
        return DEFALUT_AOD_BACK;
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalIcon() {
        return DEFAULT_NORMAL_ICON;
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalFalse() {
        return generalNormalRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalNormalBack() {
        return DEFAULT_NORMAL_BACK;
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightIcon() {
        return DEFAULT_LIGHT_ICON;
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightRecognizing() {
        return generalNormalRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightFalse() {
        return generalLightRecognizing();
    }

    /* access modifiers changed from: protected */
    public MiuiGxzwAnimRes generalLightBack() {
        return DEFAULT_LIGHT_BACK;
    }
}
