package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;

public class ExpandableViewState extends ViewState {
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dark;
    public boolean dimmed;
    public boolean headsUpIsVisible;
    public int height;
    public boolean hideSensitive;
    public boolean inShelf;
    public int location;
    public int notGoneIndex;
    public float shadowAlpha;
    public int springYOffset;

    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof ExpandableViewState) {
            ExpandableViewState expandableViewState = (ExpandableViewState) viewState;
            this.height = expandableViewState.height;
            this.dimmed = expandableViewState.dimmed;
            this.shadowAlpha = expandableViewState.shadowAlpha;
            this.dark = expandableViewState.dark;
            this.hideSensitive = expandableViewState.hideSensitive;
            this.belowSpeedBump = expandableViewState.belowSpeedBump;
            this.clipTopAmount = expandableViewState.clipTopAmount;
            this.notGoneIndex = expandableViewState.notGoneIndex;
            this.location = expandableViewState.location;
            this.headsUpIsVisible = expandableViewState.headsUpIsVisible;
        }
    }

    public void applyToView(View view) {
        super.applyToView(view);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            int actualHeight = expandableView.getActualHeight();
            int i = this.height;
            if (actualHeight != i) {
                expandableView.setActualHeight(i, false);
            }
            float shadowAlpha2 = expandableView.getShadowAlpha();
            float f = this.shadowAlpha;
            if (shadowAlpha2 != f) {
                expandableView.setShadowAlpha(f);
            }
            expandableView.setDimmed(this.dimmed, false);
            expandableView.setHideSensitive(this.hideSensitive, false, 0, 0);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            expandableView.setDark(this.dark, false, 0);
            int i2 = this.clipTopAmount;
            if (((float) expandableView.getClipTopAmount()) != ((float) i2)) {
                expandableView.setClipTopAmount(i2);
            }
            expandableView.setTransformingInShelf(false);
            expandableView.setInShelf(this.inShelf);
            if (this.headsUpIsVisible) {
                expandableView.setHeadsUpIsVisible();
            }
        }
    }

    public void animateTo(View view, AnimationProperties animationProperties) {
        super.animateTo(view, animationProperties);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            AnimationFilter animationFilter = animationProperties.getAnimationFilter();
            if (this.height != expandableView.getActualHeight()) {
                startHeightAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, R.id.height_animator_tag);
            }
            if (this.shadowAlpha != expandableView.getShadowAlpha()) {
                startShadowAlphaAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, R.id.shadow_alpha_animator_tag);
            }
            if (this.clipTopAmount != expandableView.getClipTopAmount()) {
                startInsetAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, R.id.top_inset_animator_tag);
            }
            expandableView.setDimmed(this.dimmed, false);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            expandableView.setHideSensitive(this.hideSensitive, animationFilter.animateHideSensitive, animationProperties.delay, animationProperties.duration);
            expandableView.setDark(this.dark, animationFilter.animateDark, animationProperties.delay);
            if (animationProperties.wasAdded(view) && !this.hidden) {
                expandableView.performAddAnimation(animationProperties.delay, animationProperties.duration, animationProperties.getAnimationFinishListener());
            }
            if (!expandableView.isInShelf() && this.inShelf) {
                expandableView.setTransformingInShelf(true);
            }
            expandableView.setInShelf(this.inShelf);
            if (this.headsUpIsVisible) {
                expandableView.setHeadsUpIsVisible();
            }
        }
    }

    private void startHeightAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Integer num = (Integer) ViewState.getChildTag(expandableView, R.id.height_animator_start_value_tag);
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, R.id.height_animator_end_value_tag);
        int i = this.height;
        if (num2 == null || num2.intValue() != i) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, R.id.height_animator_tag);
            if (animationProperties.getAnimationFilter().animateHeight) {
                ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{expandableView.getActualHeight(), i});
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        expandableView.setActualHeight(((Integer) valueAnimator.getAnimatedValue()).intValue(), false);
                    }
                });
                ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                ofInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofInt.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
                if (animationFinishListener != null) {
                    ofInt.addListener(animationFinishListener);
                }
                ofInt.addListener(new AnimatorListenerAdapter() {
                    boolean mWasCancelled;

                    public void onAnimationEnd(Animator animator) {
                        expandableView.setTag(R.id.height_animator_tag, (Object) null);
                        expandableView.setTag(R.id.height_animator_start_value_tag, (Object) null);
                        expandableView.setTag(R.id.height_animator_end_value_tag, (Object) null);
                        expandableView.setActualHeightAnimating(false);
                        if (!this.mWasCancelled) {
                            ExpandableView expandableView = expandableView;
                            if (expandableView instanceof ExpandableNotificationRow) {
                                ((ExpandableNotificationRow) expandableView).setExpansionChanging(false);
                                ((ExpandableNotificationRow) expandableView).setGroupExpansionChanging(false);
                            }
                        }
                    }

                    public void onAnimationStart(Animator animator) {
                        this.mWasCancelled = false;
                    }

                    public void onAnimationCancel(Animator animator) {
                        this.mWasCancelled = true;
                    }
                });
                ViewState.startAnimator(ofInt, animationFinishListener);
                expandableView.setTag(R.id.height_animator_tag, ofInt);
                expandableView.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(expandableView.getActualHeight()));
                expandableView.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(i));
                expandableView.setActualHeightAnimating(true);
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(new int[]{intValue, i});
                expandableView.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(intValue));
                expandableView.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                expandableView.setActualHeight(i, false);
            }
        }
    }

    private void startShadowAlphaAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Float f = (Float) ViewState.getChildTag(expandableView, R.id.shadow_alpha_animator_start_value_tag);
        Float f2 = (Float) ViewState.getChildTag(expandableView, R.id.shadow_alpha_animator_end_value_tag);
        float f3 = this.shadowAlpha;
        if (f2 == null || f2.floatValue() != f3) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, R.id.shadow_alpha_animator_tag);
            if (animationProperties.getAnimationFilter().animateShadowAlpha) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{expandableView.getShadowAlpha(), f3});
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        expandableView.setShadowAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                ofFloat.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofFloat.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
                if (animationFinishListener != null) {
                    ofFloat.addListener(animationFinishListener);
                }
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        expandableView.setTag(R.id.shadow_alpha_animator_tag, (Object) null);
                        expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, (Object) null);
                        expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, (Object) null);
                    }
                });
                ViewState.startAnimator(ofFloat, animationFinishListener);
                expandableView.setTag(R.id.shadow_alpha_animator_tag, ofFloat);
                expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(expandableView.getShadowAlpha()));
                expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(f3));
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                float floatValue = f.floatValue() + (f3 - f2.floatValue());
                values[0].setFloatValues(new float[]{floatValue, f3});
                expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(floatValue));
                expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(f3));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                expandableView.setShadowAlpha(f3);
            }
        }
    }

    private void startInsetAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        Integer num = (Integer) ViewState.getChildTag(expandableView, R.id.top_inset_animator_start_value_tag);
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, R.id.top_inset_animator_end_value_tag);
        int i = this.clipTopAmount;
        if (num2 == null || num2.intValue() != i) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, R.id.top_inset_animator_tag);
            if (animationProperties.getAnimationFilter().animateTopInset) {
                ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{expandableView.getClipTopAmount(), i});
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        expandableView.setClipTopAmount(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                ofInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofInt.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener();
                if (animationFinishListener != null) {
                    ofInt.addListener(animationFinishListener);
                }
                ofInt.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        expandableView.setTag(R.id.top_inset_animator_tag, (Object) null);
                        expandableView.setTag(R.id.top_inset_animator_start_value_tag, (Object) null);
                        expandableView.setTag(R.id.top_inset_animator_end_value_tag, (Object) null);
                    }
                });
                ViewState.startAnimator(ofInt, animationFinishListener);
                expandableView.setTag(R.id.top_inset_animator_tag, ofInt);
                expandableView.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(expandableView.getClipTopAmount()));
                expandableView.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(i));
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i - num2.intValue());
                values[0].setIntValues(new int[]{intValue, i});
                expandableView.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(intValue));
                expandableView.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(i));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                expandableView.setClipTopAmount(i);
            }
        }
    }

    public static int getFinalActualHeight(ExpandableView expandableView) {
        if (expandableView == null) {
            return 0;
        }
        if (((ValueAnimator) ViewState.getChildTag(expandableView, R.id.height_animator_tag)) == null) {
            return expandableView.getActualHeight();
        }
        return ((Integer) ViewState.getChildTag(expandableView, R.id.height_animator_end_value_tag)).intValue();
    }
}
