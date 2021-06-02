package com.android.systemui.qs.tileimpl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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

public class MiuiQSIconViewImpl extends QSIconView {
    private boolean mAnimationEnabled = true;
    private ObjectAnimator mAnimator;
    protected final View mIcon;
    protected final int mIconBgSizePx;
    protected int mIconColorDisabled;
    protected int mIconColorEnabled;
    protected final int mIconSizePx;
    private boolean mIsCustomTile = false;

    /* access modifiers changed from: protected */
    public int getIconMeasureMode() {
        return 1073741824;
    }

    public MiuiQSIconViewImpl(Context context) {
        super(context);
        Resources resources = context.getResources();
        this.mIconSizePx = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_icon_size);
        this.mIconBgSizePx = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_icon_bg_size);
        resources.getDimensionPixelSize(C0012R$dimen.qs_tile_padding_below_icon);
        this.mIconColorEnabled = resources.getColor(C0011R$color.qs_tile_icon_enabled_color);
        this.mIconColorDisabled = resources.getColor(C0011R$color.qs_tile_icon_disabled_color);
        View createIcon = createIcon();
        this.mIcon = createIcon;
        addView(createIcon);
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
        int size = View.MeasureSpec.getSize(i);
        this.mIcon.measure(View.MeasureSpec.makeMeasureSpec(size, getIconMeasureMode()), exactly(this.mIconBgSizePx));
        setMeasuredDimension(size, this.mIcon.getMeasuredHeight());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layout(this.mIcon, (getMeasuredWidth() - this.mIcon.getMeasuredWidth()) / 2, 0);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void updateResources() {
        Resources resources = getResources();
        this.mIconColorEnabled = resources.getColor(C0011R$color.qs_tile_icon_enabled_color);
        this.mIconColorDisabled = resources.getColor(C0011R$color.qs_tile_icon_disabled_color);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) {
        setIcon((ImageView) this.mIcon, state, z);
    }

    /* access modifiers changed from: protected */
    public void setIcon(ImageView imageView, QSTile.State state, boolean z) {
        updateIcon(imageView, state, z);
    }

    /* access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, QSTile.State state, boolean z) {
        int i;
        Supplier<QSTile.Icon> supplier = state.iconSupplier;
        QSTile.Icon icon = supplier != null ? supplier.get() : state.icon;
        if (!Objects.equals(icon, imageView.getTag(C0015R$id.qs_icon_tag))) {
            imageView.setTag(C0015R$id.qs_icon_tag, icon);
            Drawable drawable = icon != null ? icon.getDrawable(((ViewGroup) this).mContext) : null;
            if (drawable != null) {
                boolean z2 = drawable instanceof AnimatedVectorDrawable;
                if (z2) {
                    drawable.mutate();
                }
                drawable.setAutoMirrored(false);
                if (state.state == 2) {
                    i = this.mIconColorEnabled;
                } else {
                    i = this.mIconColorDisabled;
                }
                drawable.setTint(i);
                boolean z3 = state.state == 2;
                boolean z4 = imageView.getTag(C0015R$id.qs_icon_state_tag) != null ? ((Integer) imageView.getTag(C0015R$id.qs_icon_state_tag)).intValue() == 2 : z3;
                imageView.setTag(C0015R$id.qs_icon_state_tag, Integer.valueOf(state.state));
                boolean z5 = z && imageView.isShown() && this.mAnimationEnabled && imageView.getTag(C0015R$id.qs_icon_tag) != null && z4 != z3;
                int properIconSize = getProperIconSize(drawable);
                Drawable drawable2 = getResources().getDrawable(C0013R$drawable.ic_qs_bg_disabled);
                Drawable drawable3 = getResources().getDrawable(C0013R$drawable.ic_qs_bg_enabled);
                if (!z3) {
                    drawable3 = drawable2;
                }
                if (z5) {
                    int i2 = z3 ? 255 : 0;
                    Drawable mutate = getResources().getDrawable(C0013R$drawable.ic_qs_bg_enabled).mutate();
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable2, mutate, drawable});
                    layerDrawable.setLayerGravity(2, 17);
                    layerDrawable.setLayerSize(2, properIconSize, properIconSize);
                    imageView.setImageDrawable(layerDrawable);
                    startAnimation(mutate, i2);
                    if (z2) {
                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                        if (animatedVectorDrawable.isRunning()) {
                            animatedVectorDrawable.stop();
                            animatedVectorDrawable.reset();
                        }
                        animatedVectorDrawable.start();
                        return;
                    }
                    return;
                }
                LayerDrawable combine = DrawableUtils.combine(drawable3, drawable, 17);
                combine.setLayerSize(1, properIconSize, properIconSize);
                imageView.setImageDrawable(combine);
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
        duration.setInterpolator(MiuiInterpolators.EXP_EASE_OUT);
        this.mAnimator.start();
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIsCustomTile(boolean z) {
        this.mIsCustomTile = z;
    }

    private int getProperIconSize(Drawable drawable) {
        if (this.mIsCustomTile) {
            return this.mIconSizePx;
        }
        if (drawable instanceof AnimatedVectorDrawable) {
            return drawable.getIntrinsicWidth();
        }
        return this.mIconSizePx;
    }

    /* access modifiers changed from: protected */
    public View createIcon() {
        ImageView imageView = new ImageView(((ViewGroup) this).mContext);
        imageView.setId(16908294);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    /* access modifiers changed from: protected */
    public final int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    /* access modifiers changed from: protected */
    public final void layout(View view, int i, int i2) {
        view.layout(i, i2, view.getMeasuredWidth() + i, view.getMeasuredHeight() + i2);
    }
}
