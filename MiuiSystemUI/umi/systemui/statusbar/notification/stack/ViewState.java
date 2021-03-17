package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ViewState extends MiuiViewStateBase implements Dumpable {
    protected static final AnimationProperties NO_NEW_ANIMATIONS = new AnimationProperties() {
        /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass1 */
        AnimationFilter mAnimationFilter = new AnimationFilter();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    private static final AnimatableProperty SCALE_X_PROPERTY = new AnimatableProperty() {
        /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimationStartTag() {
            return C0015R$id.scale_x_animator_start_value_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimationEndTag() {
            return C0015R$id.scale_x_animator_end_value_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimatorTag() {
            return C0015R$id.scale_x_animator_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public Property getProperty() {
            return View.SCALE_X;
        }
    };
    private static final AnimatableProperty SCALE_Y_PROPERTY = new AnimatableProperty() {
        /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass3 */

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimationStartTag() {
            return C0015R$id.scale_y_animator_start_value_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimationEndTag() {
            return C0015R$id.scale_y_animator_end_value_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public int getAnimatorTag() {
            return C0015R$id.scale_y_animator_tag;
        }

        @Override // com.android.systemui.statusbar.notification.AnimatableProperty
        public Property getProperty() {
            return View.SCALE_Y;
        }
    };
    private static final int TAG_ANIMATOR_ALPHA = C0015R$id.alpha_animator_tag;
    private static final int TAG_ANIMATOR_TRANSLATION_X = C0015R$id.translation_x_animator_tag;
    private static final int TAG_ANIMATOR_TRANSLATION_Y = C0015R$id.translation_y_animator_tag;
    private static final int TAG_ANIMATOR_TRANSLATION_Z = C0015R$id.translation_z_animator_tag;
    private static final int TAG_END_ALPHA = C0015R$id.alpha_animator_end_value_tag;
    private static final int TAG_END_TRANSLATION_X = C0015R$id.translation_x_animator_end_value_tag;
    private static final int TAG_END_TRANSLATION_Y = C0015R$id.translation_y_animator_end_value_tag;
    private static final int TAG_END_TRANSLATION_Z = C0015R$id.translation_z_animator_end_value_tag;
    private static final int TAG_START_ALPHA = C0015R$id.alpha_animator_start_value_tag;
    private static final int TAG_START_TRANSLATION_X = C0015R$id.translation_x_animator_start_value_tag;
    private static final int TAG_START_TRANSLATION_Y = C0015R$id.translation_y_animator_start_value_tag;
    private static final int TAG_START_TRANSLATION_Z = C0015R$id.translation_z_animator_start_value_tag;
    public float alpha;
    public boolean gone;
    public boolean hidden;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float xTranslation;
    public float yTranslation;
    public float zTranslation;

    public void copyFrom(ViewState viewState) {
        this.alpha = viewState.alpha;
        this.xTranslation = viewState.xTranslation;
        this.yTranslation = viewState.yTranslation;
        this.zTranslation = viewState.zTranslation;
        this.gone = viewState.gone;
        this.hidden = viewState.hidden;
        this.scaleX = viewState.scaleX;
        this.scaleY = viewState.scaleY;
    }

    public void initFrom(View view) {
        this.alpha = view.getAlpha();
        this.xTranslation = view.getTranslationX();
        this.yTranslation = view.getTranslationY();
        this.zTranslation = view.getTranslationZ();
        boolean z = true;
        this.gone = view.getVisibility() == 8;
        if (view.getVisibility() != 4) {
            z = false;
        }
        this.hidden = z;
        this.scaleX = view.getScaleX();
        this.scaleY = view.getScaleY();
    }

    public void applyToView(View view) {
        if (!this.gone) {
            if (isAnimating(view, TAG_ANIMATOR_TRANSLATION_X)) {
                updateAnimationX(view);
            } else {
                float translationX = view.getTranslationX();
                float f = this.xTranslation;
                if (translationX != f) {
                    view.setTranslationX(f);
                }
            }
            if (isAnimating(view, TAG_ANIMATOR_TRANSLATION_Y)) {
                updateAnimationY(view);
            } else {
                float translationY = view.getTranslationY();
                float f2 = this.yTranslation;
                if (translationY != f2) {
                    view.setTranslationY(f2);
                }
            }
            if (isAnimating(view, TAG_ANIMATOR_TRANSLATION_Z)) {
                updateAnimationZ(view);
            } else {
                float translationZ = view.getTranslationZ();
                float f3 = this.zTranslation;
                if (translationZ != f3) {
                    view.setTranslationZ(f3);
                }
            }
            if (isAnimating(view, SCALE_X_PROPERTY)) {
                updateAnimation(view, SCALE_X_PROPERTY, this.scaleX);
            } else {
                float scaleX2 = view.getScaleX();
                float f4 = this.scaleX;
                if (scaleX2 != f4) {
                    view.setScaleX(f4);
                }
            }
            if (isAnimating(view, SCALE_Y_PROPERTY)) {
                updateAnimation(view, SCALE_Y_PROPERTY, this.scaleY);
            } else {
                float scaleY2 = view.getScaleY();
                float f5 = this.scaleY;
                if (scaleY2 != f5) {
                    view.setScaleY(f5);
                }
            }
            int visibility = view.getVisibility();
            boolean z = true;
            int i = 0;
            boolean z2 = this.alpha == 0.0f || (this.hidden && (!isAnimating(view) || visibility != 0));
            if (isAnimating(view, TAG_ANIMATOR_ALPHA)) {
                updateAlphaAnimation(view);
            } else {
                float alpha2 = view.getAlpha();
                float f6 = this.alpha;
                if (alpha2 != f6) {
                    boolean z3 = f6 == 1.0f;
                    if (z2 || z3 || !view.hasOverlappingRendering()) {
                        z = false;
                    }
                    int layerType = view.getLayerType();
                    int i2 = z ? 2 : 0;
                    if (layerType != i2) {
                        view.setLayerType(i2, null);
                    }
                    view.setAlpha(this.alpha);
                }
            }
            if (z2) {
                i = 4;
            }
            if (i == visibility) {
                return;
            }
            if (!(view instanceof ExpandableView) || !((ExpandableView) view).willBeGone()) {
                view.setVisibility(i);
            }
        }
    }

    public boolean isAnimating(View view) {
        if (!isAnimating(view, TAG_ANIMATOR_TRANSLATION_X) && !isAnimating(view, TAG_ANIMATOR_TRANSLATION_Y) && !isAnimating(view, TAG_ANIMATOR_TRANSLATION_Z) && !isAnimating(view, TAG_ANIMATOR_ALPHA) && !isAnimating(view, SCALE_X_PROPERTY) && !isAnimating(view, SCALE_Y_PROPERTY)) {
            return false;
        }
        return true;
    }

    private static boolean isAnimating(View view, int i) {
        return getChildTag(view, i) != null;
    }

    public static boolean isAnimating(View view, AnimatableProperty animatableProperty) {
        return getChildTag(view, animatableProperty.getAnimatorTag()) != null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.MiuiViewStateBase
    public void animateTo(View view, AnimationProperties animationProperties) {
        super.animateTo(view, animationProperties);
        boolean z = false;
        boolean z2 = view.getVisibility() == 0;
        float f = this.alpha;
        if (!z2 && (!(f == 0.0f && view.getAlpha() == 0.0f) && !this.gone && !this.hidden)) {
            view.setVisibility(0);
        }
        if (this.alpha != view.getAlpha()) {
            z = true;
        }
        if (view instanceof ExpandableView) {
            z &= !((ExpandableView) view).willBeGone();
        }
        if (view.getTranslationX() != this.xTranslation) {
            startXTranslationAnimation(view, animationProperties);
        } else {
            abortAnimation(view, TAG_ANIMATOR_TRANSLATION_X);
        }
        if (view.getTranslationY() != this.yTranslation) {
            startYTranslationAnimation(view, animationProperties);
        } else {
            abortAnimation(view, TAG_ANIMATOR_TRANSLATION_Y);
        }
        if (view.getTranslationZ() != this.zTranslation) {
            startZTranslationAnimation(view, animationProperties);
        } else {
            abortAnimation(view, TAG_ANIMATOR_TRANSLATION_Z);
        }
        float scaleX2 = view.getScaleX();
        float f2 = this.scaleX;
        if (scaleX2 != f2) {
            PropertyAnimator.startAnimation(view, SCALE_X_PROPERTY, f2, animationProperties);
        } else {
            abortAnimation(view, SCALE_X_PROPERTY.getAnimatorTag());
        }
        float scaleY2 = view.getScaleY();
        float f3 = this.scaleY;
        if (scaleY2 != f3) {
            PropertyAnimator.startAnimation(view, SCALE_Y_PROPERTY, f3, animationProperties);
        } else {
            abortAnimation(view, SCALE_Y_PROPERTY.getAnimatorTag());
        }
        if (z) {
            startAlphaAnimation(view, animationProperties);
        } else {
            abortAnimation(view, TAG_ANIMATOR_ALPHA);
        }
    }

    private void updateAlphaAnimation(View view) {
        startAlphaAnimation(view, NO_NEW_ANIMATIONS);
    }

    private void startAlphaAnimation(final View view, AnimationProperties animationProperties) {
        Float f = (Float) getChildTag(view, TAG_START_ALPHA);
        Float f2 = (Float) getChildTag(view, TAG_END_ALPHA);
        final float f3 = this.alpha;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, TAG_ANIMATOR_ALPHA);
            if (!animationProperties.getAnimationFilter().animateAlpha) {
                if (objectAnimator != null) {
                    PropertyValuesHolder[] values = objectAnimator.getValues();
                    float floatValue = f.floatValue() + (f3 - f2.floatValue());
                    values[0].setFloatValues(floatValue, f3);
                    view.setTag(TAG_START_ALPHA, Float.valueOf(floatValue));
                    view.setTag(TAG_END_ALPHA, Float.valueOf(f3));
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                view.setAlpha(f3);
                if (f3 == 0.0f) {
                    view.setVisibility(4);
                }
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), f3);
            Interpolator customInterpolator = animationProperties.getCustomInterpolator(view, View.ALPHA);
            if (customInterpolator == null) {
                customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            }
            ofFloat.setInterpolator(customInterpolator);
            view.setLayerType(2, null);
            ofFloat.addListener(new AnimatorListenerAdapter(this) {
                /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass4 */
                public boolean mWasCancelled;

                public void onAnimationEnd(Animator animator) {
                    view.setLayerType(0, null);
                    if (f3 == 0.0f && !this.mWasCancelled && view.getVisibility() == 0) {
                        view.setVisibility(4);
                    }
                    view.setTag(ViewState.TAG_ANIMATOR_ALPHA, null);
                    view.setTag(ViewState.TAG_START_ALPHA, null);
                    view.setTag(ViewState.TAG_END_ALPHA, null);
                }

                public void onAnimationCancel(Animator animator) {
                    this.mWasCancelled = true;
                }

                public void onAnimationStart(Animator animator) {
                    this.mWasCancelled = false;
                }
            });
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, objectAnimator));
            if (animationProperties.delay > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(animationProperties.delay);
            }
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(View.ALPHA);
            if (animationFinishListener != null) {
                ofFloat.addListener(animationFinishListener);
            }
            startAnimator(ofFloat, animationFinishListener);
            view.setTag(TAG_ANIMATOR_ALPHA, ofFloat);
            view.setTag(TAG_START_ALPHA, Float.valueOf(view.getAlpha()));
            view.setTag(TAG_END_ALPHA, Float.valueOf(f3));
        }
    }

    private void updateAnimationZ(View view) {
        startZTranslationAnimation(view, NO_NEW_ANIMATIONS);
    }

    private void updateAnimation(View view, AnimatableProperty animatableProperty, float f) {
        PropertyAnimator.startAnimation(view, animatableProperty, f, NO_NEW_ANIMATIONS);
    }

    private void startZTranslationAnimation(final View view, AnimationProperties animationProperties) {
        Float f = (Float) getChildTag(view, TAG_START_TRANSLATION_Z);
        Float f2 = (Float) getChildTag(view, TAG_END_TRANSLATION_Z);
        float f3 = this.zTranslation;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Z);
            if (!animationProperties.getAnimationFilter().animateZ) {
                if (objectAnimator != null) {
                    PropertyValuesHolder[] values = objectAnimator.getValues();
                    float floatValue = f.floatValue() + (f3 - f2.floatValue());
                    values[0].setFloatValues(floatValue, f3);
                    view.setTag(TAG_START_TRANSLATION_Z, Float.valueOf(floatValue));
                    view.setTag(TAG_END_TRANSLATION_Z, Float.valueOf(f3));
                    objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
                    return;
                }
                view.setTranslationZ(f3);
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, view.getTranslationZ(), f3);
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, objectAnimator));
            if (animationProperties.delay > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(animationProperties.delay);
            }
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(View.TRANSLATION_Z);
            if (animationFinishListener != null) {
                ofFloat.addListener(animationFinishListener);
            }
            ofFloat.addListener(new AnimatorListenerAdapter(this) {
                /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass5 */

                public void onAnimationEnd(Animator animator) {
                    view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Z, null);
                    view.setTag(ViewState.TAG_START_TRANSLATION_Z, null);
                    view.setTag(ViewState.TAG_END_TRANSLATION_Z, null);
                }
            });
            startAnimator(ofFloat, animationFinishListener);
            view.setTag(TAG_ANIMATOR_TRANSLATION_Z, ofFloat);
            view.setTag(TAG_START_TRANSLATION_Z, Float.valueOf(view.getTranslationZ()));
            view.setTag(TAG_END_TRANSLATION_Z, Float.valueOf(f3));
        }
    }

    private void updateAnimationX(View view) {
        startXTranslationAnimation(view, NO_NEW_ANIMATIONS);
    }

    private void startXTranslationAnimation(final View view, AnimationProperties animationProperties) {
        Float f = (Float) getChildTag(view, TAG_START_TRANSLATION_X);
        Float f2 = (Float) getChildTag(view, TAG_END_TRANSLATION_X);
        float f3 = this.xTranslation;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_X);
            if (animationProperties.getAnimationFilter().animateX) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getTranslationX(), f3);
                Interpolator customInterpolator = animationProperties.getCustomInterpolator(view, View.TRANSLATION_X);
                if (customInterpolator == null) {
                    customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
                }
                ofFloat.setInterpolator(customInterpolator);
                ofFloat.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, objectAnimator));
                if (animationProperties.delay > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                    ofFloat.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(View.TRANSLATION_X);
                if (animationFinishListener != null) {
                    ofFloat.addListener(animationFinishListener);
                }
                ofFloat.addListener(new AnimatorListenerAdapter(this) {
                    /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass6 */

                    public void onAnimationEnd(Animator animator) {
                        view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_X, null);
                        view.setTag(ViewState.TAG_START_TRANSLATION_X, null);
                        view.setTag(ViewState.TAG_END_TRANSLATION_X, null);
                    }
                });
                startAnimator(ofFloat, animationFinishListener);
                view.setTag(TAG_ANIMATOR_TRANSLATION_X, ofFloat);
                view.setTag(TAG_START_TRANSLATION_X, Float.valueOf(view.getTranslationX()));
                view.setTag(TAG_END_TRANSLATION_X, Float.valueOf(f3));
            } else if (objectAnimator != null) {
                PropertyValuesHolder[] values = objectAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(floatValue, f3);
                view.setTag(TAG_START_TRANSLATION_X, Float.valueOf(floatValue));
                view.setTag(TAG_END_TRANSLATION_X, Float.valueOf(f3));
                objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
            } else {
                view.setTranslationX(f3);
            }
        }
    }

    private void updateAnimationY(View view) {
        startYTranslationAnimation(view, NO_NEW_ANIMATIONS);
    }

    private void startYTranslationAnimation(final View view, AnimationProperties animationProperties) {
        Float f = (Float) getChildTag(view, TAG_START_TRANSLATION_Y);
        Float f2 = (Float) getChildTag(view, TAG_END_TRANSLATION_Y);
        float f3 = this.yTranslation;
        if (f2 == null || f2.floatValue() != f3) {
            ObjectAnimator objectAnimator = (ObjectAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Y);
            if (animationProperties.getAnimationFilter().shouldAnimateY(view)) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), f3);
                Interpolator customInterpolator = animationProperties.getCustomInterpolator(view, View.TRANSLATION_Y);
                if (customInterpolator == null) {
                    customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
                }
                ofFloat.setInterpolator(customInterpolator);
                ofFloat.setDuration(cancelAnimatorAndGetNewDuration(animationProperties.duration, objectAnimator));
                if (animationProperties.delay > 0 && (objectAnimator == null || objectAnimator.getAnimatedFraction() == 0.0f)) {
                    ofFloat.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(View.TRANSLATION_Y);
                if (animationFinishListener != null) {
                    ofFloat.addListener(animationFinishListener);
                }
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.statusbar.notification.stack.ViewState.AnonymousClass7 */

                    public void onAnimationEnd(Animator animator) {
                        HeadsUpUtil.setIsClickedHeadsUpNotification(view, false);
                        view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Y, null);
                        view.setTag(ViewState.TAG_START_TRANSLATION_Y, null);
                        view.setTag(ViewState.TAG_END_TRANSLATION_Y, null);
                        ViewState.this.onYTranslationAnimationFinished(view);
                    }
                });
                startAnimator(ofFloat, animationFinishListener);
                view.setTag(TAG_ANIMATOR_TRANSLATION_Y, ofFloat);
                view.setTag(TAG_START_TRANSLATION_Y, Float.valueOf(view.getTranslationY()));
                view.setTag(TAG_END_TRANSLATION_Y, Float.valueOf(f3));
            } else if (objectAnimator != null) {
                PropertyValuesHolder[] values = objectAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(floatValue, f3);
                view.setTag(TAG_START_TRANSLATION_Y, Float.valueOf(floatValue));
                view.setTag(TAG_END_TRANSLATION_Y, Float.valueOf(f3));
                objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
            } else {
                view.setTranslationY(f3);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onYTranslationAnimationFinished(View view) {
        if (this.hidden && !this.gone) {
            view.setVisibility(4);
        }
    }

    public static void startAnimator(Animator animator, AnimatorListenerAdapter animatorListenerAdapter) {
        if (animatorListenerAdapter != null) {
            animatorListenerAdapter.onAnimationStart(animator);
        }
        animator.start();
    }

    public static <T> T getChildTag(View view, int i) {
        return (T) view.getTag(i);
    }

    /* access modifiers changed from: protected */
    public void abortAnimation(View view, int i) {
        Animator animator = (Animator) getChildTag(view, i);
        if (animator != null) {
            animator.cancel();
        }
    }

    public static long cancelAnimatorAndGetNewDuration(long j, ValueAnimator valueAnimator) {
        if (valueAnimator == null) {
            return j;
        }
        long max = Math.max(valueAnimator.getDuration() - valueAnimator.getCurrentPlayTime(), j);
        valueAnimator.cancel();
        return max;
    }

    public static float getFinalTranslationX(View view) {
        if (view == null) {
            return 0.0f;
        }
        if (((ValueAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_X)) == null) {
            return view.getTranslationX();
        }
        return ((Float) getChildTag(view, TAG_END_TRANSLATION_X)).floatValue();
    }

    public static float getFinalTranslationY(View view) {
        if (view == null) {
            return 0.0f;
        }
        if (((ValueAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Y)) == null) {
            return view.getTranslationY();
        }
        return ((Float) getChildTag(view, TAG_END_TRANSLATION_Y)).floatValue();
    }

    public static float getFinalTranslationZ(View view) {
        if (view == null) {
            return 0.0f;
        }
        if (((ValueAnimator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Z)) == null) {
            return view.getTranslationZ();
        }
        return ((Float) getChildTag(view, TAG_END_TRANSLATION_Z)).floatValue();
    }

    public static boolean isAnimatingY(View view) {
        return getChildTag(view, TAG_ANIMATOR_TRANSLATION_Y) != null;
    }

    public void cancelAnimations(View view) {
        Animator animator = (Animator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_X);
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = (Animator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Y);
        if (animator2 != null) {
            animator2.cancel();
        }
        Animator animator3 = (Animator) getChildTag(view, TAG_ANIMATOR_TRANSLATION_Z);
        if (animator3 != null) {
            animator3.cancel();
        }
        Animator animator4 = (Animator) getChildTag(view, TAG_ANIMATOR_ALPHA);
        if (animator4 != null) {
            animator4.cancel();
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append("ViewState { ");
        boolean z = true;
        for (Class<?> cls = getClass(); cls != null; cls = cls.getSuperclass()) {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !field.isSynthetic() && !Modifier.isTransient(modifiers)) {
                    if (!z) {
                        sb.append(", ");
                    }
                    try {
                        sb.append(field.getName());
                        sb.append(": ");
                        field.setAccessible(true);
                        sb.append(field.get(this));
                    } catch (IllegalAccessException unused) {
                    }
                    z = false;
                }
            }
        }
        sb.append(" }");
        printWriter.print(sb);
    }
}
