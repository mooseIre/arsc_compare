package com.android.systemui.qs;

import android.content.res.ColorStateList;
import com.android.systemui.qs.tileimpl.SlashImageView;

public class AlphaControlledSignalTileView$AlphaControlledSlashImageView extends SlashImageView {
    public void setFinalImageTintList(ColorStateList colorStateList) {
        super.setImageTintList(colorStateList);
        SlashDrawable slash = getSlash();
        if (slash != null) {
            ((AlphaControlledSignalTileView$AlphaControlledSlashDrawable) slash).setFinalTintList(colorStateList);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.SlashImageView
    public void ensureSlashDrawable() {
        if (getSlash() == null) {
            AlphaControlledSignalTileView$AlphaControlledSlashDrawable alphaControlledSignalTileView$AlphaControlledSlashDrawable = new AlphaControlledSignalTileView$AlphaControlledSlashDrawable(getDrawable());
            setSlash(alphaControlledSignalTileView$AlphaControlledSlashDrawable);
            alphaControlledSignalTileView$AlphaControlledSlashDrawable.setAnimationEnabled(getAnimationEnabled());
            setImageViewDrawable(alphaControlledSignalTileView$AlphaControlledSlashDrawable);
        }
    }
}
