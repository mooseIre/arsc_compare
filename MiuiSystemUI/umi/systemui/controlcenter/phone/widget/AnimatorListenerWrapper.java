package com.android.systemui.controlcenter.phone.widget;

import android.animation.Animator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimatorListenerWrapper implements Animator.AnimatorListener {
    private List<Animator.AnimatorListener> mListeners;

    private AnimatorListenerWrapper(Animator.AnimatorListener... animatorListenerArr) {
        ArrayList arrayList = new ArrayList();
        this.mListeners = arrayList;
        Collections.addAll(arrayList, animatorListenerArr);
    }

    public void onAnimationStart(Animator animator) {
        for (Animator.AnimatorListener animatorListener : this.mListeners) {
            animatorListener.onAnimationStart(animator);
        }
    }

    public void onAnimationEnd(Animator animator) {
        for (Animator.AnimatorListener animatorListener : this.mListeners) {
            animatorListener.onAnimationEnd(animator);
        }
    }

    public void onAnimationCancel(Animator animator) {
        for (Animator.AnimatorListener animatorListener : this.mListeners) {
            animatorListener.onAnimationCancel(animator);
        }
    }

    public void onAnimationRepeat(Animator animator) {
        for (Animator.AnimatorListener animatorListener : this.mListeners) {
            animatorListener.onAnimationRepeat(animator);
        }
    }

    public static Animator.AnimatorListener of(Animator.AnimatorListener... animatorListenerArr) {
        return new AnimatorListenerWrapper(animatorListenerArr);
    }
}
