package com.android.systemui.controlcenter.phone.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HideBeforeAnimatorListener extends AnimatorListenerAdapter {
    private int mAlphaDuration = 300;
    private Interpolator mAlphaInterpolator;
    private boolean mAnimateAlpha;
    protected final List<View> mViews;

    public HideBeforeAnimatorListener(View... viewArr) {
        ArrayList arrayList = new ArrayList();
        this.mViews = arrayList;
        Collections.addAll(arrayList, viewArr);
    }

    public void onAnimationStart(Animator animator) {
        super.onAnimationStart(animator);
        for (View view : this.mViews) {
            apply(view);
        }
    }

    public HideBeforeAnimatorListener animateAlpha(boolean z) {
        this.mAnimateAlpha = z;
        return this;
    }

    public HideBeforeAnimatorListener setAlphaDuration(int i) {
        this.mAlphaDuration = i;
        return this;
    }

    public HideBeforeAnimatorListener setAlphaInterpolator(Interpolator interpolator) {
        this.mAlphaInterpolator = interpolator;
        return this;
    }

    private void apply(View view) {
        if (this.mAnimateAlpha) {
            view.animate().cancel();
            view.animate().alpha(0.0f).setDuration((long) this.mAlphaDuration).setInterpolator(this.mAlphaInterpolator).withLayer().start();
            return;
        }
        view.setVisibility(8);
    }
}
