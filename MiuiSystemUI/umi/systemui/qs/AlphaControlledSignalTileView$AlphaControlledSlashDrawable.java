package com.android.systemui.qs;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

public class AlphaControlledSignalTileView$AlphaControlledSlashDrawable extends SlashDrawable {
    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.SlashDrawable
    public void setDrawableTintList(ColorStateList colorStateList) {
    }

    AlphaControlledSignalTileView$AlphaControlledSlashDrawable(Drawable drawable) {
        super(drawable);
    }

    public void setFinalTintList(ColorStateList colorStateList) {
        super.setDrawableTintList(colorStateList);
    }
}
