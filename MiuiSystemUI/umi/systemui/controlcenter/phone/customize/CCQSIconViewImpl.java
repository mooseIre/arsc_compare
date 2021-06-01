package com.android.systemui.controlcenter.phone.customize;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.miui.systemui.graphics.DrawableUtils;
import com.miui.systemui.util.MiuiInterpolators;
import java.util.Objects;
import java.util.function.Supplier;

public class CCQSIconViewImpl extends QSIconView {
    private boolean mAnimationEnabled;
    private ObjectAnimator mAnimator;
    private int mCustomTileSize;
    protected final View mIcon;
    protected int mIconColor;
    protected int mIconColorOff;
    protected int mIconColorUnavailable;
    private boolean mIsCustomTile;
    private QSTile.State mState;
    private int mTileSize;

    public CCQSIconViewImpl(Context context) {
        this(context, null);
    }

    public CCQSIconViewImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAnimationEnabled = true;
        this.mIsCustomTile = false;
        this.mIconColor = getResources().getColor(C0011R$color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(C0011R$color.cc_qs_tile_icon_color_off);
        this.mIconColorUnavailable = getResources().getColor(C0011R$color.qs_control_tile_icon_unavailable_color);
        this.mCustomTileSize = (int) getResources().getDimension(C0012R$dimen.qs_control_custom_tile_icon_inner_size);
        this.mTileSize = (int) getResources().getDimension(C0012R$dimen.qs_control_center_tile_width);
        this.mState = new QSTile.State();
        View createIcon = createIcon();
        this.mIcon = createIcon;
        addView(createIcon);
        updateResources();
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setAnimationEnabled(boolean z) {
        this.mAnimationEnabled = z;
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public View getIconView() {
        return this.mIcon;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mIcon.layout(0, 0, i3 - i, i4 - i2);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void updateResources() {
        this.mIconColor = getResources().getColor(C0011R$color.cc_qs_tile_icon_color);
        this.mIconColorOff = getResources().getColor(C0011R$color.cc_qs_tile_icon_color_off);
        this.mIconColorUnavailable = getResources().getColor(C0011R$color.qs_control_tile_icon_unavailable_color);
        this.mCustomTileSize = (int) getResources().getDimension(C0012R$dimen.qs_control_custom_tile_icon_inner_size);
        updateIcon((ImageView) this.mIcon, this.mState, true);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) {
        setIcon((ImageView) this.mIcon, state);
    }

    /* access modifiers changed from: protected */
    public void setIcon(ImageView imageView, QSTile.State state) {
        this.mState = state;
        updateIcon(imageView, state);
    }

    /* access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, QSTile.State state) {
        updateIcon(imageView, state, false);
    }

    private void updateIcon(ImageView imageView, QSTile.State state, boolean z) {
        int i;
        Drawable drawable;
        int i2;
        Supplier<QSTile.Icon> supplier = state.iconSupplier;
        QSTile.Icon icon = supplier != null ? supplier.get() : state.icon;
        Drawable drawable2 = icon != null ? icon.getDrawable(((ViewGroup) this).mContext) : null;
        if (drawable2 != null) {
            Integer num = (Integer) imageView.getTag(C0015R$id.qs_icon_state_tag);
            if (z || num == null || num.intValue() != state.state || !Objects.equals(icon, imageView.getTag(C0015R$id.qs_icon_tag))) {
                drawable2.mutate();
                drawable2.setAutoMirrored(false);
                if (state.activeBgColor != 2) {
                    int i3 = state.state;
                    if (i3 == 2) {
                        drawable2.setTint(this.mIconColor);
                    } else if (i3 == 1) {
                        drawable2.setTint(this.mIconColorOff);
                    } else if (i3 == 0) {
                        drawable2.setTint(this.mIconColorUnavailable);
                    }
                }
                if (num == null) {
                    i = -1;
                } else {
                    i = num.intValue();
                }
                int properIconSize = getProperIconSize(drawable2);
                if ((!this.mAnimationEnabled || (i2 = state.state) == 0 || (i == 0 && i2 == 1) || i == state.state) ? false : true) {
                    int i4 = state.state == 2 ? 255 : 0;
                    Drawable drawable3 = getResources().getDrawable(C0013R$drawable.ic_cc_qs_bg_inactive);
                    Drawable mutate = getResources().getDrawable(getActiveBgDrawable(state)).mutate();
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable3, mutate, drawable2});
                    layerDrawable.setLayerGravity(2, 17);
                    layerDrawable.setLayerSize(2, properIconSize, properIconSize);
                    imageView.setImageDrawable(layerDrawable);
                    startAnimation(mutate, i4);
                    if (drawable2 instanceof AnimatedVectorDrawable) {
                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable2;
                        animatedVectorDrawable.mutate();
                        if (animatedVectorDrawable.isRunning()) {
                            animatedVectorDrawable.stop();
                            animatedVectorDrawable.reset();
                        }
                        animatedVectorDrawable.start();
                    }
                } else {
                    int i5 = state.state;
                    if (i5 == 0) {
                        drawable = getResources().getDrawable(C0013R$drawable.ic_cc_qs_bg_unavailable);
                    } else if (i5 == 2) {
                        drawable = getResources().getDrawable(getActiveBgDrawable(state));
                    } else {
                        drawable = getResources().getDrawable(C0013R$drawable.ic_cc_qs_bg_inactive);
                    }
                    if (drawable2 instanceof AnimatedVectorDrawable) {
                        AnimatedVectorDrawable animatedVectorDrawable2 = (AnimatedVectorDrawable) drawable2;
                        animatedVectorDrawable2.mutate();
                        if (animatedVectorDrawable2.isRunning()) {
                            animatedVectorDrawable2.stop();
                            animatedVectorDrawable2.reset();
                        }
                        animatedVectorDrawable2.start();
                        animatedVectorDrawable2.stop();
                    }
                    LayerDrawable combine = DrawableUtils.combine(drawable, drawable2, 17);
                    combine.setLayerSize(1, properIconSize, properIconSize);
                    imageView.setImageDrawable(combine);
                }
                imageView.setTag(C0015R$id.qs_icon_state_tag, Integer.valueOf(state.state));
                imageView.setTag(C0015R$id.qs_icon_tag, icon);
            }
        }
    }

    private void startAnimation(Drawable drawable, int i) {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
            this.mAnimator.removeAllListeners();
            this.mAnimator.removeAllUpdateListeners();
            this.mAnimator = null;
        }
        ObjectAnimator duration = ObjectAnimator.ofInt(drawable, "alpha", 255 - i, i).setDuration(300L);
        this.mAnimator = duration;
        duration.setInterpolator(MiuiInterpolators.CUBIC_EASE_OUT);
        this.mAnimator.start();
    }

    private int getActiveBgDrawable(QSTile.State state) {
        int i = state.activeBgColor;
        if (i == 1) {
            return C0013R$drawable.ic_cc_qs_bg_active_battery_related;
        }
        if (i != 2) {
            return C0013R$drawable.ic_cc_qs_bg_active_normal;
        }
        return C0013R$drawable.ic_cc_qs_bg_active_auto_brightness;
    }

    private int getProperIconSize(Drawable drawable) {
        if (this.mIsCustomTile) {
            return this.mCustomTileSize;
        }
        return drawable instanceof AnimatedVectorDrawable ? this.mTileSize : this.mCustomTileSize;
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIsCustomTile(boolean z) {
        this.mIsCustomTile = z;
    }

    /* access modifiers changed from: protected */
    public View createIcon() {
        ImageView imageView = new ImageView(((ViewGroup) this).mContext);
        imageView.setId(16908294);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }
}
