package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.internal.util.ContrastColorUtil;

public class CompatibilityColorUtil {
    private final ContrastColorUtil mContrastColorUtil;

    public CompatibilityColorUtil(Context context) {
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
    }

    public boolean isGrayscaleIcon(Drawable drawable) {
        return this.mContrastColorUtil.isGrayscaleIcon(drawable);
    }

    public static boolean isColorLight(int i) {
        return ContrastColorUtil.isColorLight(i);
    }

    public static CharSequence clearColorSpans(CharSequence charSequence) {
        return ContrastColorUtil.clearColorSpans(charSequence);
    }
}
