package com.android.systemui.miui.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class HideAfterAnimatorListener extends AnimatorListenerAdapter {
    private boolean mCanceled;
    private View mView;

    public HideAfterAnimatorListener(View view) {
        this.mView = view;
    }

    public void onAnimationCancel(Animator animator) {
        this.mCanceled = true;
    }

    public void onAnimationEnd(Animator animator) {
        if (!this.mCanceled) {
            this.mView.setVisibility(8);
        }
    }
}
