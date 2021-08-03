package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.policy.HeadsUpUtil;

public class ExpandableViewState extends ViewState {
    private static final int TAG_ANIMATOR_HEIGHT = C0015R$id.height_animator_tag;
    private static final int TAG_ANIMATOR_TOP_INSET = C0015R$id.top_inset_animator_tag;
    private static final int TAG_END_HEIGHT = C0015R$id.height_animator_end_value_tag;
    private static final int TAG_END_TOP_INSET = C0015R$id.top_inset_animator_end_value_tag;
    private static final int TAG_START_HEIGHT = C0015R$id.height_animator_start_value_tag;
    private static final int TAG_START_TOP_INSET = C0015R$id.top_inset_animator_start_value_tag;
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dimmed;
    public boolean headsUpIsVisible;
    public int height;
    public boolean hideSensitive;
    public boolean inShelf;
    public int location;
    public int notGoneIndex;

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof ExpandableViewState) {
            ExpandableViewState expandableViewState = (ExpandableViewState) viewState;
            this.height = expandableViewState.height;
            this.dimmed = expandableViewState.dimmed;
            this.hideSensitive = expandableViewState.hideSensitive;
            this.belowSpeedBump = expandableViewState.belowSpeedBump;
            this.clipTopAmount = expandableViewState.clipTopAmount;
            this.notGoneIndex = expandableViewState.notGoneIndex;
            this.location = expandableViewState.location;
            this.headsUpIsVisible = expandableViewState.headsUpIsVisible;
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void applyToView(View view) {
        super.applyToView(view);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            int actualHeight = expandableView.getActualHeight();
            int i = this.height;
            if (actualHeight != i) {
                expandableView.setActualHeight(i, false);
            }
            expandableView.setDimmed(this.dimmed, false);
            expandableView.setHideSensitive(this.hideSensitive, false, 0, 0);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
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

    @Override // com.android.systemui.statusbar.notification.stack.ViewState, com.android.systemui.statusbar.notification.stack.MiuiViewStateBase
    public void animateTo(View view, AnimationProperties animationProperties) {
        super.animateTo(view, animationProperties);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            AnimationFilter animationFilter = animationProperties.getAnimationFilter();
            if (this.height != expandableView.getActualHeight()) {
                startHeightAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, TAG_ANIMATOR_HEIGHT);
            }
            if (this.clipTopAmount != expandableView.getClipTopAmount()) {
                startInsetAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, TAG_ANIMATOR_TOP_INSET);
            }
            expandableView.setDimmed(this.dimmed, animationFilter.animateDimmed);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            expandableView.setHideSensitive(this.hideSensitive, animationFilter.animateHideSensitive, animationProperties.delay, animationProperties.duration);
            if (view.getTag(FoldManager.Companion.getTagId()) != null) {
                view.setTag(FoldManager.Companion.getTagId(), null);
            } else if (animationProperties.wasAdded(view) && !this.hidden) {
                expandableView.performAddAnimation(animationProperties.delay, animationProperties.duration, false);
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

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void startYTranslationAnimationUnimportant(View view, AnimationProperties animationProperties) {
        float f;
        Float f2 = (Float) ViewState.getChildTag(view, FoldManager.Companion.getTagId());
        if (f2 != null && (view instanceof ExpandableView) && !((ExpandableView) view).isChildInGroup()) {
            abortAnimation(view, ViewState.TAG_ANIMATOR_TRANSLATION_Y);
            view.clearAnimation();
            view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Y, null);
            boolean wasAdded = animationProperties.wasAdded(view);
            if (!FoldManager.Companion.isFoldNeedsAnim()) {
                foldWithoutAnim(view, wasAdded);
                return;
            }
            if (FoldManager.Companion.isShowingUnimportant()) {
                f = FoldManager.Companion.getUnimportantTarget();
            } else {
                f = FoldManager.Companion.getNormalTarget();
            }
            float f3 = f - 50.0f;
            if (wasAdded) {
                float floatValue = this.yTranslation + f2.floatValue();
                if (f2.floatValue() < 0.0f && floatValue >= f3) {
                    floatValue = f3;
                }
                view.setTranslationY(floatValue);
            } else {
                this.yTranslation -= f2.floatValue();
                if (f2.floatValue() > 0.0f && this.yTranslation >= f3) {
                    this.yTranslation = f3;
                }
            }
            float f4 = this.yTranslation;
            float translationY = view.getTranslationY();
            float max = Math.max(Math.min(translationY, f4), f3);
            foldWithAnim(view, animationProperties, wasAdded, getUpdateListener(view, f3, Math.abs(Math.max(translationY, f4) - max), max, (wasAdded && f2.floatValue() < 0.0f) || (!wasAdded && f2.floatValue() > 0.0f), wasAdded));
        }
    }

    private ValueAnimator.AnimatorUpdateListener getUpdateListener(View view, float f, float f2, float f3, boolean z, boolean z2) {
        return new ValueAnimator.AnimatorUpdateListener(f2, f, f3, z, z2, view) {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$ExpandableViewState$pqjb7BtGcbTn0FdL9fyN88I8qo */
            public final /* synthetic */ float f$0;
            public final /* synthetic */ float f$1;
            public final /* synthetic */ float f$2;
            public final /* synthetic */ boolean f$3;
            public final /* synthetic */ boolean f$4;
            public final /* synthetic */ View f$5;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ExpandableViewState.lambda$getUpdateListener$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, valueAnimator);
            }
        };
    }

    static /* synthetic */ void lambda$getUpdateListener$0(float f, float f2, float f3, boolean z, boolean z2, View view, ValueAnimator valueAnimator) {
        float f4;
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        if (!Float.isNaN(floatValue)) {
            if (f == 0.0f || floatValue < f2) {
                f4 = 0.0f;
            } else {
                f4 = Math.min(Math.max((floatValue - f3) / f, 0.0f), 1.0f);
                if (!z) {
                    f4 = 1.0f - f4;
                }
            }
            if (z2 || view.getTransitionAlpha() != 0.0f) {
                view.setTransitionAlpha(f4);
            }
            view.setTranslationY(floatValue);
        }
    }

    private void foldWithAnim(final View view, AnimationProperties animationProperties, final boolean z, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        Float f = (Float) ViewState.getChildTag(view, ViewState.TAG_END_TRANSLATION_Y);
        float f2 = this.yTranslation;
        if (f == null || f.floatValue() != f2) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(view, ViewState.TAG_ANIMATOR_TRANSLATION_Y);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(view.getTranslationY(), f2);
            Interpolator customInterpolator = animationProperties.getCustomInterpolator(view, View.TRANSLATION_Y);
            if (customInterpolator == null) {
                customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            }
            ofFloat.setInterpolator(customInterpolator);
            ofFloat.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
            if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                ofFloat.setStartDelay(animationProperties.delay);
            }
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(View.TRANSLATION_Y);
            if (animationFinishListener != null) {
                ofFloat.addListener(animationFinishListener);
            }
            ofFloat.addUpdateListener(animatorUpdateListener);
            ofFloat.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.statusbar.notification.stack.ExpandableViewState.AnonymousClass1 */

                public void onAnimationEnd(Animator animator) {
                    HeadsUpUtil.setIsClickedHeadsUpNotification(view, false);
                    view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Y, null);
                    view.setTag(ViewState.TAG_START_TRANSLATION_Y, null);
                    view.setTag(ViewState.TAG_END_TRANSLATION_Y, null);
                    ExpandableViewState.this.onYTranslationAnimationFinished(view);
                    if (z) {
                        view.setTransitionAlpha(1.0f);
                    } else {
                        StackStateAnimator.removeTransientView((ExpandableView) view);
                    }
                }
            });
            ViewState.startAnimator(ofFloat, animationFinishListener);
            view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Y, ofFloat);
            view.setTag(ViewState.TAG_START_TRANSLATION_Y, Float.valueOf(view.getTranslationY()));
            view.setTag(ViewState.TAG_END_TRANSLATION_Y, Float.valueOf(f2));
        }
    }

    private void foldWithoutAnim(View view, boolean z) {
        ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(view, ViewState.TAG_ANIMATOR_TRANSLATION_Y);
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        HeadsUpUtil.setIsClickedHeadsUpNotification(view, false);
        view.setTag(ViewState.TAG_ANIMATOR_TRANSLATION_Y, null);
        view.setTag(ViewState.TAG_START_TRANSLATION_Y, null);
        view.setTag(ViewState.TAG_END_TRANSLATION_Y, null);
        onYTranslationAnimationFinished(view);
        if (z) {
            view.setTranslationY(this.yTranslation);
            view.setTransitionAlpha(1.0f);
            return;
        }
        StackStateAnimator.removeTransientView((ExpandableView) view);
    }

    private void startHeightAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        int i = TAG_ANIMATOR_HEIGHT;
        int i2 = TAG_END_HEIGHT;
        int i3 = TAG_START_HEIGHT;
        Integer num = (Integer) ViewState.getChildTag(expandableView, i3);
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, i2);
        int i4 = this.height;
        if (num2 == null || num2.intValue() != i4) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, i);
            if (animationProperties.getAnimationFilter().animateHeight) {
                ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getActualHeight(), i4);
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) {
                    /* class com.android.systemui.statusbar.notification.stack.ExpandableViewState.AnonymousClass2 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        expandableView.setActualHeight(((Integer) valueAnimator.getAnimatedValue()).intValue(), false);
                    }
                });
                ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                ofInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofInt.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(null);
                if (animationFinishListener != null) {
                    ofInt.addListener(animationFinishListener);
                }
                ofInt.addListener(new AnimatorListenerAdapter(this) {
                    /* class com.android.systemui.statusbar.notification.stack.ExpandableViewState.AnonymousClass3 */
                    boolean mWasCancelled;

                    public void onAnimationEnd(Animator animator) {
                        expandableView.setTag(ExpandableViewState.TAG_ANIMATOR_HEIGHT, null);
                        expandableView.setTag(ExpandableViewState.TAG_START_HEIGHT, null);
                        expandableView.setTag(ExpandableViewState.TAG_END_HEIGHT, null);
                        expandableView.setActualHeightAnimating(false);
                        if (!this.mWasCancelled) {
                            ExpandableView expandableView = expandableView;
                            if (expandableView instanceof ExpandableNotificationRow) {
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
                expandableView.setTag(i, ofInt);
                expandableView.setTag(i3, Integer.valueOf(expandableView.getActualHeight()));
                expandableView.setTag(i2, Integer.valueOf(i4));
                expandableView.setActualHeightAnimating(true);
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i4 - num2.intValue());
                values[0].setIntValues(intValue, i4);
                expandableView.setTag(i3, Integer.valueOf(intValue));
                expandableView.setTag(i2, Integer.valueOf(i4));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                expandableView.setActualHeight(i4, false);
            }
        }
    }

    private void startInsetAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        int i = TAG_ANIMATOR_TOP_INSET;
        int i2 = TAG_END_TOP_INSET;
        int i3 = TAG_START_TOP_INSET;
        Integer num = (Integer) ViewState.getChildTag(expandableView, i3);
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, i2);
        int i4 = this.clipTopAmount;
        if (num2 == null || num2.intValue() != i4) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, i);
            if (animationProperties.getAnimationFilter().animateTopInset) {
                ValueAnimator ofInt = ValueAnimator.ofInt(expandableView.getClipTopAmount(), i4);
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) {
                    /* class com.android.systemui.statusbar.notification.stack.ExpandableViewState.AnonymousClass4 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        expandableView.setClipTopAmount(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                ofInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
                if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                    ofInt.setStartDelay(animationProperties.delay);
                }
                AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(null);
                if (animationFinishListener != null) {
                    ofInt.addListener(animationFinishListener);
                }
                ofInt.addListener(new AnimatorListenerAdapter(this) {
                    /* class com.android.systemui.statusbar.notification.stack.ExpandableViewState.AnonymousClass5 */

                    public void onAnimationEnd(Animator animator) {
                        expandableView.setTag(ExpandableViewState.TAG_ANIMATOR_TOP_INSET, null);
                        expandableView.setTag(ExpandableViewState.TAG_START_TOP_INSET, null);
                        expandableView.setTag(ExpandableViewState.TAG_END_TOP_INSET, null);
                    }
                });
                ViewState.startAnimator(ofInt, animationFinishListener);
                expandableView.setTag(i, ofInt);
                expandableView.setTag(i3, Integer.valueOf(expandableView.getClipTopAmount()));
                expandableView.setTag(i2, Integer.valueOf(i4));
            } else if (valueAnimator != null) {
                PropertyValuesHolder[] values = valueAnimator.getValues();
                int intValue = num.intValue() + (i4 - num2.intValue());
                values[0].setIntValues(intValue, i4);
                expandableView.setTag(i3, Integer.valueOf(intValue));
                expandableView.setTag(i2, Integer.valueOf(i4));
                valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            } else {
                expandableView.setClipTopAmount(i4);
            }
        }
    }

    public static int getFinalActualHeight(ExpandableView expandableView) {
        if (expandableView == null) {
            return 0;
        }
        if (((ValueAnimator) ViewState.getChildTag(expandableView, TAG_ANIMATOR_HEIGHT)) == null) {
            return expandableView.getActualHeight();
        }
        return ((Integer) ViewState.getChildTag(expandableView, TAG_END_HEIGHT)).intValue();
    }

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void cancelAnimations(View view) {
        super.cancelAnimations(view);
        Animator animator = (Animator) ViewState.getChildTag(view, TAG_ANIMATOR_HEIGHT);
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = (Animator) ViewState.getChildTag(view, TAG_ANIMATOR_TOP_INSET);
        if (animator2 != null) {
            animator2.cancel();
        }
    }
}
