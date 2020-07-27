package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.android.systemui.Constants;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.property.FloatProperty;

public class StackStateAnimator {
    private static final boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    public AnimationFilter mAnimationFilter = new AnimationFilter();
    /* access modifiers changed from: private */
    public Stack<AnimatorListenerAdapter> mAnimationListenerPool = new Stack<>();
    private final AnimationProperties mAnimationProperties;
    /* access modifiers changed from: private */
    public HashSet<Animator> mAnimatorSet = new HashSet<>();
    /* access modifiers changed from: private */
    public HashSet<View> mAppearingChildren = new HashSet<>();
    /* access modifiers changed from: private */
    public ValueAnimator mBottomOverScrollAnimator;
    private ArrayList<View> mChildrenToClearFromOverlay = new ArrayList<>();
    private long mCurrentAdditionalDelay;
    private int mCurrentLastNotAddedIndex;
    private long mCurrentLength;
    /* access modifiers changed from: private */
    public HashSet<View> mDisappearingChildren = new HashSet<>();
    private final int mGoToFullShadeAppearingTranslation;
    /* access modifiers changed from: private */
    public HashSet<View> mHeadsUpAppearChildren = new HashSet<>();
    private int mHeadsUpAppearHeightBottom;
    /* access modifiers changed from: private */
    public HashSet<View> mHeadsUpDisappearChildren = new HashSet<>();
    public NotificationStackScrollLayout mHostLayout;
    /* access modifiers changed from: private */
    public ArrayList<View> mNewAddChildren = new ArrayList<>();
    private ArrayList<NotificationStackScrollLayout.AnimationEvent> mNewEvents = new ArrayList<>();
    private boolean mShadeExpanded;
    private NotificationShelf mShelf;
    private final ExpandableViewState mTmpState = new ExpandableViewState();
    /* access modifiers changed from: private */
    public ValueAnimator mTopOverScrollAnimator;

    public StackStateAnimator(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mHostLayout = notificationStackScrollLayout;
        this.mGoToFullShadeAppearingTranslation = notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(R.dimen.go_to_full_shade_appearing_translation);
        this.mAnimationProperties = new AnimationProperties() {
            public AnimationFilter getAnimationFilter() {
                return StackStateAnimator.this.mAnimationFilter;
            }

            public AnimatorListenerAdapter getAnimationFinishListener() {
                return StackStateAnimator.this.getGlobalAnimationFinishedListener();
            }

            public boolean wasAdded(View view) {
                return StackStateAnimator.this.mNewAddChildren.contains(view);
            }

            public Interpolator getCustomInterpolator(View view, Property property) {
                if (StackStateAnimator.this.mHeadsUpAppearChildren.contains(view) && View.TRANSLATION_Y.equals(property)) {
                    return Interpolators.HEADS_UP_APPEAR;
                }
                if (StackStateAnimator.this.mHeadsUpDisappearChildren.contains(view) && View.TRANSLATION_Y.equals(property)) {
                    return Interpolators.HEADS_UP_DISAPPEAR;
                }
                if (StackStateAnimator.this.mAppearingChildren.contains(view) || StackStateAnimator.this.mDisappearingChildren.contains(view)) {
                    return Interpolators.APPEAR_DISAPPEAR;
                }
                return null;
            }
        };
    }

    public boolean isRunning() {
        return !this.mAnimatorSet.isEmpty();
    }

    public void startAnimationForEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, StackScrollState stackScrollState, long j) {
        processAnimationEvents(arrayList, stackScrollState);
        int childCount = this.mHostLayout.getChildCount();
        this.mAnimationFilter.applyCombination(this.mNewEvents);
        this.mCurrentAdditionalDelay = j;
        this.mCurrentLength = NotificationStackScrollLayout.AnimationEvent.combineLength(this.mNewEvents);
        this.mCurrentLastNotAddedIndex = findLastNotAddedIndex(stackScrollState);
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostLayout.getChildAt(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (!(viewStateForView == null || expandableView.getVisibility() == 8 || applyWithoutAnimation(expandableView, viewStateForView, stackScrollState))) {
                initAnimationProperties(stackScrollState, expandableView, viewStateForView);
                viewStateForView.animateTo(expandableView, this.mAnimationProperties);
            }
        }
        if (!isRunning()) {
            onAnimationFinished();
        }
        this.mHeadsUpAppearChildren.clear();
        this.mHeadsUpDisappearChildren.clear();
        this.mAppearingChildren.clear();
        this.mDisappearingChildren.clear();
        this.mNewEvents.clear();
        this.mNewAddChildren.clear();
    }

    private void initAnimationProperties(StackScrollState stackScrollState, ExpandableView expandableView, ExpandableViewState expandableViewState) {
        boolean wasAdded = this.mAnimationProperties.wasAdded(expandableView);
        this.mAnimationProperties.duration = this.mCurrentLength;
        adaptDurationWhenGoingToFullShade(expandableView, expandableViewState, wasAdded);
        this.mAnimationProperties.delay = 0;
        if (!wasAdded) {
            if (!this.mAnimationFilter.hasDelays) {
                return;
            }
            if (expandableViewState.yTranslation == expandableView.getTranslationY() && expandableViewState.zTranslation == expandableView.getTranslationZ() && expandableViewState.alpha == expandableView.getAlpha() && expandableViewState.height == expandableView.getActualHeight() && expandableViewState.clipTopAmount == expandableView.getClipTopAmount() && expandableViewState.dark == expandableView.isDark() && expandableViewState.shadowAlpha == expandableView.getShadowAlpha()) {
                return;
            }
        }
        this.mAnimationProperties.delay = this.mCurrentAdditionalDelay + calculateChildAnimationDelay(expandableViewState, stackScrollState);
    }

    private void adaptDurationWhenGoingToFullShade(ExpandableView expandableView, ExpandableViewState expandableViewState, boolean z) {
        if (z && this.mAnimationFilter.hasGoToFullShadeEvent) {
            expandableView.setTranslationY(expandableView.getTranslationY() + ((float) this.mGoToFullShadeAppearingTranslation));
            this.mAnimationProperties.duration = ((long) (((float) Math.pow((double) ((float) (expandableViewState.notGoneIndex - this.mCurrentLastNotAddedIndex)), 0.699999988079071d)) * 100.0f)) + 514;
        }
    }

    private boolean applyWithoutAnimation(ExpandableView expandableView, ExpandableViewState expandableViewState, StackScrollState stackScrollState) {
        if (this.mShadeExpanded || ViewState.isAnimatingY(expandableView) || this.mHeadsUpDisappearChildren.contains(expandableView) || this.mHeadsUpAppearChildren.contains(expandableView) || this.mAppearingChildren.contains(expandableView) || this.mDisappearingChildren.contains(expandableView) || NotificationStackScrollLayout.isPinnedHeadsUp(expandableView)) {
            return false;
        }
        expandableViewState.applyToView(expandableView);
        return true;
    }

    private int findLastNotAddedIndex(StackScrollState stackScrollState) {
        for (int childCount = this.mHostLayout.getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) this.mHostLayout.getChildAt(childCount);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (viewStateForView != null && expandableView.getVisibility() != 8 && !this.mNewAddChildren.contains(expandableView)) {
                return viewStateForView.notGoneIndex;
            }
        }
        return -1;
    }

    private long calculateChildAnimationDelay(ExpandableViewState expandableViewState, StackScrollState stackScrollState) {
        AnimationFilter animationFilter = this.mAnimationFilter;
        if (animationFilter.hasGoToFullShadeEvent) {
            return calculateDelayGoToFullShade(expandableViewState);
        }
        if (animationFilter.hasHeadsUpDisappearClickEvent) {
            return 120;
        }
        Iterator<NotificationStackScrollLayout.AnimationEvent> it = this.mNewEvents.iterator();
        while (true) {
            long j = 0;
            while (it.hasNext()) {
                NotificationStackScrollLayout.AnimationEvent next = it.next();
                int i = next.animationType;
                if (i == 0) {
                    int i2 = expandableViewState.notGoneIndex;
                    if (stackScrollState.getViewStateForView(next.changingView) != null) {
                        j = Math.max(((long) (2 - Math.max(0, Math.min(2, Math.abs(i2 - stackScrollState.getViewStateForView(next.changingView).notGoneIndex) - 1)))) * 80, j);
                    }
                } else if (i == 1 || i == 2) {
                }
            }
            return j;
        }
    }

    private long calculateDelayGoToFullShade(ExpandableViewState expandableViewState) {
        int notGoneIndex = this.mShelf.getNotGoneIndex();
        float f = (float) expandableViewState.notGoneIndex;
        float f2 = (float) notGoneIndex;
        long j = 0;
        if (f > f2) {
            j = 0 + ((long) (((double) (((float) Math.pow((double) (f - f2), 0.699999988079071d)) * 48.0f)) * 0.25d));
        } else {
            f2 = f;
        }
        return j + ((long) (((float) Math.pow((double) f2, 0.699999988079071d)) * 48.0f));
    }

    /* access modifiers changed from: private */
    public AnimatorListenerAdapter getGlobalAnimationFinishedListener() {
        if (!this.mAnimationListenerPool.empty()) {
            return this.mAnimationListenerPool.pop();
        }
        return new AnimatorListenerAdapter() {
            private boolean mWasCancelled;

            public void onAnimationEnd(Animator animator) {
                StackStateAnimator.this.mAnimatorSet.remove(animator);
                if (StackStateAnimator.this.mAnimatorSet.isEmpty() && !this.mWasCancelled) {
                    StackStateAnimator.this.onAnimationFinished();
                }
                StackStateAnimator.this.mAnimationListenerPool.push(this);
            }

            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }

            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
                StackStateAnimator.this.mAnimatorSet.add(animator);
            }
        };
    }

    /* access modifiers changed from: private */
    public void onAnimationFinished() {
        this.mHostLayout.onChildAnimationFinished();
        Iterator<View> it = this.mChildrenToClearFromOverlay.iterator();
        while (it.hasNext()) {
            removeFromOverlay(it.next());
        }
        this.mChildrenToClearFromOverlay.clear();
    }

    private void processAnimationEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, StackScrollState stackScrollState) {
        float f;
        Iterator<NotificationStackScrollLayout.AnimationEvent> it = arrayList.iterator();
        while (it.hasNext()) {
            NotificationStackScrollLayout.AnimationEvent next = it.next();
            View view = next.changingView;
            final ExpandableView expandableView = (ExpandableView) view;
            int i = next.animationType;
            if (i == 0) {
                ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
                if (viewStateForView != null) {
                    viewStateForView.applyToView(expandableView);
                    this.mNewAddChildren.add(expandableView);
                }
            } else if (i == 1) {
                if (expandableView.getVisibility() != 0) {
                    removeFromOverlay(expandableView);
                } else {
                    ExpandableViewState viewStateForView2 = stackScrollState.getViewStateForView(next.viewAfterChangingView);
                    int actualHeight = expandableView.getActualHeight();
                    if (viewStateForView2 != null) {
                        float translationY = expandableView.getTranslationY();
                        if (expandableView instanceof ExpandableNotificationRow) {
                            View view2 = next.viewAfterChangingView;
                            if (view2 instanceof ExpandableNotificationRow) {
                                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) view2;
                                if (expandableNotificationRow.isRemoved() && expandableNotificationRow.wasChildInGroupWhenRemoved() && !expandableNotificationRow2.isChildInGroup()) {
                                    translationY = expandableNotificationRow.getTranslationWhenRemoved();
                                }
                            }
                        }
                        float f2 = (float) actualHeight;
                        f = Math.max(Math.min(((viewStateForView2.yTranslation - (translationY + (f2 / 2.0f))) * 2.0f) / f2, 1.0f), -1.0f);
                    } else {
                        f = -1.0f;
                    }
                    expandableView.performRemoveAnimation(464, f, this.mAnimationProperties.getAnimationFinishListener(), new Runnable() {
                        public void run() {
                            StackStateAnimator.removeFromOverlay(expandableView);
                        }
                    });
                }
            } else if (i == 2) {
                this.mHostLayout.getOverlay().remove(expandableView);
                if (Math.abs(expandableView.getTranslation()) == ((float) expandableView.getWidth()) && expandableView.getTransientContainer() != null) {
                    expandableView.getTransientContainer().removeTransientView(expandableView);
                }
            } else if (i == 13) {
                ((ExpandableNotificationRow) view).prepareExpansionChanged(stackScrollState);
            } else if (i == 14) {
                this.mTmpState.copyFrom(stackScrollState.getViewStateForView(expandableView));
                if (next.headsUpFromBottom) {
                    this.mTmpState.yTranslation = (float) this.mHeadsUpAppearHeightBottom;
                } else {
                    ExpandableViewState expandableViewState = this.mTmpState;
                    expandableViewState.yTranslation = (float) (-expandableViewState.height);
                }
                this.mHeadsUpAppearChildren.add(expandableView);
                this.mTmpState.applyToView(expandableView);
            } else if (i == 15 || i == 16) {
                this.mHeadsUpDisappearChildren.add(expandableView);
                if (expandableView.getParent() == null) {
                    this.mHostLayout.getOverlay().add(expandableView);
                    this.mTmpState.initFrom(expandableView);
                    this.mTmpState.yTranslation = (float) (-expandableView.getActualHeight());
                    this.mAnimationFilter.animateY = true;
                    this.mAnimationProperties.delay = next.animationType == 16 ? 120 : 0;
                    AnimationProperties animationProperties = this.mAnimationProperties;
                    animationProperties.duration = 150;
                    this.mTmpState.animateTo(expandableView, animationProperties);
                    this.mChildrenToClearFromOverlay.add(expandableView);
                }
            } else if (i == 18) {
                this.mAppearingChildren.add(expandableView);
            } else if (i == 19) {
                this.mDisappearingChildren.add(expandableView);
            } else if (i == 20) {
                animateSpringReset(expandableView);
            }
            this.mNewEvents.add(next);
        }
    }

    private void animateSpringReset(final ExpandableView expandableView) {
        if (expandableView.getViewState() != null) {
            int intValue = ((Integer) expandableView.getTag(R.id.view_index_tag)).intValue();
            float f = (float) expandableView.getViewState().springYOffset;
            if (DEBUG) {
                Log.d("StackStateAnimator", "animateSpringReset i=" + intValue + ", springYOffset start=" + f);
            }
            final String str = "RowSpringReset-" + intValue;
            expandableView.setTag(R.id.folme_spring_reset, str);
            Folme.getValueTarget(str).setMinVisibleChange(1.0f, "offset");
            IStateStyle useValue = Folme.useValue(str);
            useValue.setTo("offset", Float.valueOf(f));
            float f2 = (float) intValue;
            useValue.setConfig(newSpringEase((0.04f * f2) + 0.7f, (f2 * 0.02f) + 0.5f), new FloatProperty[0]);
            useValue.addListener(new AutoCleanFloatTransitionListener(str) {
                public void onUpdate(Map<String, Float> map) {
                    float floatValue = map.get("offset").floatValue();
                    StackStateAnimator.this.mHostLayout.onChildrenSpringAnimationUpdate();
                    if (Math.abs(floatValue) < 0.5f) {
                        floatValue = 0.0f;
                        onEnd();
                        Folme.useValue(str).cancel();
                    }
                    if (expandableView.getViewState() != null) {
                        expandableView.getViewState().springYOffset = (int) floatValue;
                    }
                }

                public void onEnd() {
                    expandableView.setTag(R.id.folme_spring_reset, (Object) null);
                    StackStateAnimator.this.onFolmeAnimationEnd();
                }
            });
            useValue.to("offset", 0);
        }
    }

    private AnimConfig newSpringEase(float f, float f2) {
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, f, f2);
        return animConfig;
    }

    private boolean isFolmeAnimating() {
        int childCount = this.mHostLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (ViewState.isFolmeAnimating(this.mHostLayout.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onFolmeAnimationEnd() {
        if (!isFolmeAnimating()) {
            this.mHostLayout.onChildAnimationFinished();
        }
    }

    public static void removeFromOverlay(View view) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(view);
        }
    }

    public void animateOverScrollToAmount(float f, final boolean z, final boolean z2) {
        float currentOverScrollAmount = this.mHostLayout.getCurrentOverScrollAmount(z);
        if (f != currentOverScrollAmount) {
            cancelOverScrollAnimators(z);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{currentOverScrollAmount, f});
            ofFloat.setDuration(360);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    StackStateAnimator.this.mHostLayout.setOverScrollAmount(((Float) valueAnimator.getAnimatedValue()).floatValue(), z, false, false, z2);
                }
            });
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (z) {
                        ValueAnimator unused = StackStateAnimator.this.mTopOverScrollAnimator = null;
                    } else {
                        ValueAnimator unused2 = StackStateAnimator.this.mBottomOverScrollAnimator = null;
                    }
                }
            });
            ofFloat.start();
            if (z) {
                this.mTopOverScrollAnimator = ofFloat;
            } else {
                this.mBottomOverScrollAnimator = ofFloat;
            }
        }
    }

    public void cancelOverScrollAnimators(boolean z) {
        ValueAnimator valueAnimator = z ? this.mTopOverScrollAnimator : this.mBottomOverScrollAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void setHeadsUpAppearHeightBottom(int i) {
        this.mHeadsUpAppearHeightBottom = i;
    }

    public void setShadeExpanded(boolean z) {
        this.mShadeExpanded = z;
    }

    public void setShelf(NotificationShelf notificationShelf) {
        this.mShelf = notificationShelf;
    }
}
