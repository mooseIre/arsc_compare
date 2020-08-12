package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import java.util.Objects;

public class BigQSIconViewImpl extends QSIconView {
    protected boolean mAnimationEnabled;
    protected ImageView mIcon;
    protected int mIconColor;
    protected int mIconColorOff;

    public void setIsCustomTile(boolean z) {
    }

    public BigQSIconViewImpl(Context context) {
        this(context, (AttributeSet) null);
    }

    public BigQSIconViewImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAnimationEnabled = true;
        this.mIconColor = context.getResources().getColor(R.color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(R.color.cc_qs_tile_icon_color_off);
        this.mIcon = createIcon();
        addView(this.mIcon);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mIcon.layout(0, 0, i3 - i, i4 - i2);
    }

    public void updateResources() {
        this.mIconColor = getResources().getColor(R.color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(R.color.cc_qs_tile_icon_color_off);
    }

    public void setIcon(QSTile.State state) {
        QSTile.Icon icon = state.icon;
        Drawable drawable = icon != null ? icon.getDrawable(this.mContext) : null;
        if (drawable != null) {
            Integer num = (Integer) this.mIcon.getTag(R.id.qs_icon_state_tag);
            if (num == null || num.intValue() != state.state || !Objects.equals(state.icon, this.mIcon.getTag(R.id.qs_icon_tag))) {
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
                this.mIcon.setTag(R.id.qs_icon_state_tag, Integer.valueOf(state.state));
                this.mIcon.setTag(R.id.qs_icon_tag, state.icon);
            }
        }
    }

    public void setAnimationEnabled(boolean z) {
        this.mAnimationEnabled = z;
    }

    public View getIconView() {
        return this.mIcon;
    }

    /* access modifiers changed from: protected */
    public ImageView createIcon() {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setId(16908294);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }
}
