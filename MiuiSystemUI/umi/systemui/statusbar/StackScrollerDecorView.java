package com.android.systemui.statusbar;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;

public abstract class StackScrollerDecorView extends ExpandableView {
    /* access modifiers changed from: private */
    public boolean mAnimating;
    protected View mContent;
    private boolean mIsVisible;

    /* access modifiers changed from: protected */
    public abstract View findContentView();

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isTransparent() {
        return true;
    }

    public StackScrollerDecorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        findContentView();
        this.mContent = this;
        setInvisible();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setOutlineProvider((ViewOutlineProvider) null);
    }

    public void performVisibilityAnimation(boolean z) {
        animateText(z, (Runnable) null);
    }

    private void animateText(boolean z, final Runnable runnable) {
        Interpolator interpolator;
        if (z != this.mIsVisible) {
            float f = z ? 1.0f : 0.0f;
            if (z) {
                interpolator = Interpolators.ALPHA_IN;
            } else {
                interpolator = Interpolators.ALPHA_OUT;
            }
            this.mContent.animate().alpha(f).setInterpolator(interpolator).setDuration(260).withEndAction(new Runnable() {
                public void run() {
                    boolean unused = StackScrollerDecorView.this.mAnimating = false;
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
            this.mIsVisible = z;
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void setInvisible() {
        this.mContent.setAlpha(0.0f);
        this.mIsVisible = false;
    }

    public void performRemoveAnimation(long j, float f, AnimatorListenerAdapter animatorListenerAdapter, Runnable runnable) {
        performVisibilityAnimation(false);
    }

    public void performAddAnimation(long j, long j2, AnimatorListenerAdapter animatorListenerAdapter) {
        performVisibilityAnimation(true);
    }
}
