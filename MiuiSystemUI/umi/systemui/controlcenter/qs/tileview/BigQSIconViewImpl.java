package com.android.systemui.controlcenter.qs.tileview;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import java.util.Objects;

public class BigQSIconViewImpl extends QSIconView {
    protected ImageView mIcon;
    protected int mIconColor;
    protected int mIconColorOff;

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setAnimationEnabled(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIsCustomTile(boolean z) {
    }

    public BigQSIconViewImpl(Context context) {
        this(context, null);
    }

    public BigQSIconViewImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIconColor = context.getResources().getColor(C0011R$color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(C0011R$color.cc_qs_tile_icon_color_off);
        ImageView createIcon = createIcon();
        this.mIcon = createIcon;
        addView(createIcon);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mIcon.layout(0, 0, i3 - i, i4 - i2);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void updateResources() {
        this.mIconColor = getResources().getColor(C0011R$color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(C0011R$color.cc_qs_tile_icon_color_off);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) {
        QSTile.Icon icon = state.icon;
        Drawable drawable = icon != null ? icon.getDrawable(((ViewGroup) this).mContext) : null;
        if (drawable != null) {
            Integer num = (Integer) this.mIcon.getTag(C0015R$id.qs_icon_state_tag);
            if (num == null || num.intValue() != state.state || !Objects.equals(state.icon, this.mIcon.getTag(C0015R$id.qs_icon_tag))) {
                drawable.setAutoMirrored(false);
                drawable.setTint(state.state == 2 ? this.mIconColor : this.mIconColorOff);
                if (state.state == 0) {
                    drawable.mutate();
                    drawable.setAlpha(76);
                    this.mIcon.setImageDrawable(drawable);
                } else {
                    this.mIcon.setImageDrawable(drawable);
                    if (drawable instanceof AnimatedVectorDrawable) {
                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                        animatedVectorDrawable.mutate();
                        animatedVectorDrawable.start();
                    }
                }
                this.mIcon.setTag(C0015R$id.qs_icon_state_tag, Integer.valueOf(state.state));
                this.mIcon.setTag(C0015R$id.qs_icon_tag, state.icon);
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public View getIconView() {
        return this.mIcon;
    }

    /* access modifiers changed from: protected */
    public ImageView createIcon() {
        ImageView imageView = new ImageView(((ViewGroup) this).mContext);
        imageView.setId(16908294);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }
}
