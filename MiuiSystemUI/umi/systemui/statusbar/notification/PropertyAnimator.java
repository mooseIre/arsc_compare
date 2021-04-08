package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;

public class PropertyAnimator {
    public static <T extends View> void setProperty(T t, AnimatableProperty animatableProperty, float f, AnimationProperties animationProperties, boolean z) {
        if (((ValueAnimator) ViewState.getChildTag(t, animatableProperty.getAnimatorTag())) != null || z) {
            if (!z) {
                animationProperties = null;
            }
            startAnimation(t, animatableProperty, f, animationProperties);
            return;
        }
        animatableProperty.getProperty().set(t, Float.valueOf(f));
    }

    public static <T extends View> void startAnimation(final T t, AnimatableProperty animatableProperty, float f, AnimationProperties animationProperties) {
        Property property = animatableProperty.getProperty();
        final int animationStartTag = animatableProperty.getAnimationStartTag();
        final int animationEndTag = animatableProperty.getAnimationEndTag();
        Float f2 = (Float) ViewState.getChildTag(t, animationStartTag);
        Float f3 = (Float) ViewState.getChildTag(t, animationEndTag);
        if (f3 == null || f3.floatValue() != f) {
            final int animatorTag = animatableProperty.getAnimatorTag();
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(t, animatorTag);
            AnimationFilter animationFilter = animationProperties != null ? animationProperties.getAnimationFilter() : null;
            if (animationFilter != null && animationFilter.shouldAnimateProperty(property)) {
                Float f4 = (Float) property.get(t);
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(property);
                if (f4.equals(Float.valueOf(f))) {
                    if (valueAnimator != null) {
                        valueAnimator.cancel();
                    }
                    if (animationFinishListener != null) {
                        animationFinishListener.onAnimationEnd(null);
                        return;
                    }
                    return;
                }
                ValueAnimator ofFloat = ValueAnimator.ofFloat(f4.floatValue(), f);
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(property, t) {
                    /* class com.android.systemui.statusbar.notification.$$Lambda$PropertyAnimator$VEXcQpkY9kIrKbFhOrW7gy9zN4 */
                    public final /* synthetic */ Property f$0;
                    public final /* synthetic */ View f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        PropertyAnimator.lambda$startAnimation$0(this.f$0, this.f$1, valueAnimator);
                    }
                });
                Interpolator customInterpolator = animationProperties.getCustomInterpolator(t, property);
                if (customInterpolator == null) {
                    customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
                }
                ofFloat.setInterpolator(customInterpolator);
                ofFloat.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofFloat.setStartDelay(animationProperties.delay);
                }
                if (animationFinishListener != null) {
                    ofFloat.addListener(animationFinishListener);
                }
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.statusbar.notification.PropertyAnimator.AnonymousClass1 */

                    public void onAnimationEnd(Animator animator) {
                        t.setTag(animatorTag, null);
                        t.setTag(animationStartTag, null);
                        t.setTag(animationEndTag, null);
                    }
                });
                ViewState.startAnimator(ofFloat, animationFinishListener);
                t.setTag(animatorTag, ofFloat);
                t.setTag(animationStartTag, f4);
                t.setTag(animationEndTag, Float.valueOf(f));
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                float floatValue = f2.floatValue() + (f - f3.floatValue());
                values[0].setFloatValues(floatValue, f);
                t.setTag(animationStartTag, Float.valueOf(floatValue));
                t.setTag(animationEndTag, Float.valueOf(f));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                property.set(t, Float.valueOf(f));
            }
        }
    }

    public static <T extends View> void applyImmediately(T t, AnimatableProperty animatableProperty, float f) {
        cancelAnimation(t, animatableProperty);
        animatableProperty.getProperty().set(t, Float.valueOf(f));
    }

    public static void cancelAnimation(View view, AnimatableProperty animatableProperty) {
        ValueAnimator valueAnimator = (ValueAnimator) view.getTag(animatableProperty.getAnimatorTag());
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
