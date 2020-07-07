package com.android.keyguard.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.SpringInterpolator;
import android.view.animation.TranslateYAnimation;

public class ViewAnimationUtils {
    public static Animation generalWakeupTranslateAnimation(float f) {
        TranslateYAnimation translateYAnimation = new TranslateYAnimation(f, 0.0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateYAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(700);
        animationSet.setInterpolator(new SpringInterpolator(0.95f, 0.8571f));
        return animationSet;
    }

    public static Animation generalWakeupAlphaAimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(350);
        alphaAnimation.setInterpolator(new SpringInterpolator(0.9f, 0.86f));
        return alphaAnimation;
    }

    public static Animation generalWakeupScaleAimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f, 1, 0.5f, 1, 0.5f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setDuration(350);
        animationSet.setInterpolator(new SpringInterpolator(0.9f, 0.86f));
        return animationSet;
    }
}
