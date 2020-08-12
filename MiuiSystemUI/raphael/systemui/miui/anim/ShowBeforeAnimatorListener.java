package com.android.systemui.miui.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowBeforeAnimatorListener extends AnimatorListenerAdapter {
    private int mAlphaDuration = 300;
    private Interpolator mAlphaInterpolator;
    private boolean mAnimateAlpha;
    protected final List<View> mViews = new ArrayList();

    public ShowBeforeAnimatorListener(View... viewArr) {
        Collections.addAll(this.mViews, viewArr);
    }

    public void onAnimationStart(Animator animator) {
        super.onAnimationStart(animator);
        for (View apply : this.mViews) {
            apply(apply);
        }
    }

    public ShowBeforeAnimatorListener animateAlpha(boolean z) {
        this.mAnimateAlpha = z;
        return this;
    }

    public ShowBeforeAnimatorListener setAlphaDuration(int i) {
        this.mAlphaDuration = i;
        return this;
    }

    public ShowBeforeAnimatorListener setAlphaInterpolator(Interpolator interpolator) {
        this.mAlphaInterpolator = interpolator;
        return this;
    }

    private void apply(View view) {
        if (this.mAnimateAlpha) {
            view.animate().cancel();
            view.animate().alpha(1.0f).setDuration((long) this.mAlphaDuration).setInterpolator(this.mAlphaInterpolator).withLayer().start();
            return;
        }
        view.setVisibility(0);
    }
}
