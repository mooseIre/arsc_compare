package com.android.systemui.qs.tileimpl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.DrawableUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import java.util.Objects;

public class QSIconViewImpl extends QSIconView {
    private boolean mAnimationEnabled = true;
    private ObjectAnimator mAnimator;
    protected final View mIcon;
    protected final int mIconBgSizePx;
    protected int mIconColorDisabled;
    protected int mIconColorEnabled;
    protected final int mIconSizePx;
    private boolean mIsCustomTile = false;
    protected final int mTilePaddingBelowIconPx;

    /* access modifiers changed from: protected */
    public int getIconMeasureMode() {
        return 1073741824;
    }

    public QSIconViewImpl(Context context) {
        super(context);
        Resources resources = context.getResources();
        this.mIconSizePx = resources.getDimensionPixelSize(R.dimen.qs_tile_icon_size);
        this.mIconBgSizePx = resources.getDimensionPixelSize(R.dimen.qs_tile_icon_bg_size);
        this.mTilePaddingBelowIconPx = resources.getDimensionPixelSize(R.dimen.qs_tile_padding_below_icon);
        this.mIconColorEnabled = resources.getColor(R.color.qs_tile_icon_enabled_color);
        this.mIconColorDisabled = resources.getColor(R.color.qs_tile_icon_disabled_color);
        this.mIcon = createIcon();
        addView(this.mIcon);
    }

    public void setAnimationEnabled(boolean z) {
        this.mAnimationEnabled = z;
    }

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

    public void updateResources() {
        Resources resources = getResources();
        this.mIconColorEnabled = resources.getColor(R.color.qs_tile_icon_enabled_color);
        this.mIconColorDisabled = resources.getColor(R.color.qs_tile_icon_disabled_color);
    }

    public void setIcon(QSTile.State state) {
        setIcon((ImageView) this.mIcon, state);
    }

    /* access modifiers changed from: protected */
    public void setIcon(ImageView imageView, QSTile.State state) {
        updateIcon(imageView, state);
    }

    /* access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, QSTile.State state) {
        int i;
        if (!Objects.equals(state.icon, imageView.getTag(R.id.qs_icon_tag))) {
            imageView.setTag(R.id.qs_icon_tag, state.icon);
            QSTile.Icon icon = state.icon;
            Drawable drawable = icon != null ? icon.getDrawable(this.mContext) : null;
            if (drawable != null) {
                boolean z = drawable instanceof AnimatedVectorDrawable;
                if (z) {
                    drawable.mutate();
                }
                drawable.setAutoMirrored(false);
                if (state.state == 2) {
                    i = this.mIconColorEnabled;
                } else {
                    i = this.mIconColorDisabled;
                }
                drawable.setTint(i);
                boolean z2 = state.state == 2;
                boolean z3 = imageView.getTag(R.id.qs_icon_state_tag) != null ? ((Integer) imageView.getTag(R.id.qs_icon_state_tag)).intValue() == 2 : z2;
                imageView.setTag(R.id.qs_icon_state_tag, Integer.valueOf(state.state));
                boolean z4 = imageView.isShown() && this.mAnimationEnabled && imageView.getTag(R.id.qs_icon_tag) != null && z3 != z2;
                int properIconSize = getProperIconSize(drawable);
                Drawable drawable2 = getResources().getDrawable(R.drawable.ic_qs_bg_disabled);
                Drawable drawable3 = getResources().getDrawable(R.drawable.ic_qs_bg_enabled);
                if (!z2) {
                    drawable3 = drawable2;
                }
                if (z4) {
                    int i2 = z2 ? 255 : 0;
                    Drawable mutate = getResources().getDrawable(R.drawable.ic_qs_bg_enabled).mutate();
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable2, mutate, drawable});
                    layerDrawable.setLayerGravity(2, 17);
                    layerDrawable.setLayerSize(2, properIconSize, properIconSize);
                    imageView.setImageDrawable(layerDrawable);
                    startAnimation(mutate, i2);
                    if (z) {
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
        this.mAnimator = ObjectAnimator.ofInt(drawable, "alpha", new int[]{255 - i, i}).setDuration(300);
        this.mAnimator.setInterpolator(Interpolators.CUBIC_EASE_OUT);
        this.mAnimator.start();
    }

    public void setIsCustomTile(boolean z) {
        this.mIsCustomTile = z;
    }

    private int getProperIconSize(Drawable drawable) {
        if (this.mIsCustomTile) {
            return this.mIconSizePx;
        }
        return drawable instanceof AnimatedVectorDrawable ? drawable.getIntrinsicWidth() : this.mIconSizePx;
    }

    /* access modifiers changed from: protected */
    public View createIcon() {
        ImageView imageView = new ImageView(this.mContext);
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
