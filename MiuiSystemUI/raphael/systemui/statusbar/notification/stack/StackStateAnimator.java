package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class StackStateAnimator {
    public static final int ANIMATION_DURATION_HEADS_UP_APPEAR_CLOSED = ((int) (HeadsUpAppearInterpolator.getFractionUntilOvershoot() * 550.0f));
    protected AnimationFilter mAnimationFilter = new AnimationFilter();
    private Stack<AnimatorListenerAdapter> mAnimationListenerPool = new Stack<>();
    protected final AnimationProperties mAnimationProperties;
    private HashSet<Animator> mAnimatorSet = new HashSet<>();
    private ValueAnimator mBottomOverScrollAnimator;
    private long mCurrentAdditionalDelay;
    private long mCurrentLength;
    private final int mGoToFullShadeAppearingTranslation;
    private HashSet<View> mHeadsUpAppearChildren = new HashSet<>();
    private int mHeadsUpAppearHeightBottom;
    private HashSet<View> mHeadsUpDisappearChildren = new HashSet<>();
    public NotificationStackScrollLayout mHostLayout;
    private ArrayList<View> mNewAddChildren = new ArrayList<>();
    private ArrayList<NotificationStackScrollLayout.AnimationEvent> mNewEvents = new ArrayList<>();
    private boolean mShadeExpanded;
    private NotificationShelf mShelf;
    private int[] mTmpLocation = new int[2];
    private final ExpandableViewState mTmpState = new ExpandableViewState();
    private ValueAnimator mTopOverScrollAnimator;
    protected ArrayList<ExpandableView> mTransientViewsToRemove = new ArrayList<>();

    /* access modifiers changed from: protected */
    public abstract boolean applyWithAnimation(View view);

    /* access modifiers changed from: protected */
    public Interpolator getCustomInterpolator(View view, Property property) {
        return null;
    }

    public StackStateAnimator(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mHostLayout = notificationStackScrollLayout;
        this.mGoToFullShadeAppearingTranslation = notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(C0012R$dimen.go_to_full_shade_appearing_translation);
        notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(C0012R$dimen.pulsing_notification_appear_translation);
        this.mAnimationProperties = new AnimationProperties() {
            /* class com.android.systemui.statusbar.notification.stack.StackStateAnimator.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return StackStateAnimator.this.mAnimationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimatorListenerAdapter getAnimationFinishListener(Property property) {
                return StackStateAnimator.this.getGlobalAnimationFinishedListener();
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public boolean wasAdded(View view) {
                return StackStateAnimator.this.mNewAddChildren.contains(view);
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public Interpolator getCustomInterpolator(View view, Property property) {
                if (!StackStateAnimator.this.mHeadsUpAppearChildren.contains(view) || !View.TRANSLATION_Y.equals(property)) {
                    return StackStateAnimator.this.getCustomInterpolator(view, property);
                }
                return Interpolators.HEADS_UP_APPEAR;
            }
        };
    }

    public boolean isRunning() {
        return !this.mAnimatorSet.isEmpty();
    }

    public void startAnimationForEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, long j) {
        processAnimationEvents(arrayList);
        int childCount = this.mHostLayout.getChildCount();
        this.mAnimationFilter.applyCombination(this.mNewEvents);
        this.mCurrentAdditionalDelay = j;
        this.mCurrentLength = NotificationStackScrollLayout.AnimationEvent.combineLength(this.mNewEvents);
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            ExpandableView expandableView = (ExpandableView) this.mHostLayout.getChildAt(i2);
            ExpandableViewState viewState = expandableView.getViewState();
            if (!(viewState == null || expandableView.getVisibility() == 8 || applyWithoutAnimation(expandableView, viewState))) {
                if (this.mAnimationProperties.wasAdded(expandableView) && i < 5) {
                    i++;
                }
                initAnimationProperties(expandableView, viewState, i);
                viewState.animateTo(expandableView, this.mAnimationProperties);
            }
        }
        if (!isRunning()) {
            onAnimationFinished();
        }
        this.mHeadsUpAppearChildren.clear();
        this.mHeadsUpDisappearChildren.clear();
        this.mNewEvents.clear();
        this.mNewAddChildren.clear();
    }

    private void initAnimationProperties(ExpandableView expandableView, ExpandableViewState expandableViewState, int i) {
        boolean wasAdded = this.mAnimationProperties.wasAdded(expandableView);
        this.mAnimationProperties.duration = this.mCurrentLength;
        adaptDurationWhenGoingToFullShade(expandableView, expandableViewState, wasAdded, i);
        this.mAnimationProperties.delay = 0;
        if (!wasAdded) {
            if (!this.mAnimationFilter.hasDelays) {
                return;
            }
            if (expandableViewState.yTranslation == expandableView.getTranslationY() && expandableViewState.zTranslation == expandableView.getTranslationZ() && expandableViewState.alpha == expandableView.getAlpha() && expandableViewState.height == expandableView.getActualHeight() && expandableViewState.clipTopAmount == expandableView.getClipTopAmount()) {
                return;
            }
        }
        this.mAnimationProperties.delay = this.mCurrentAdditionalDelay + calculateChildAnimationDelay(expandableViewState, i);
    }

    private void adaptDurationWhenGoingToFullShade(ExpandableView expandableView, ExpandableViewState expandableViewState, boolean z, int i) {
        if (z && this.mAnimationFilter.hasGoToFullShadeEvent) {
            expandableView.setTranslationY(expandableView.getTranslationY() + ((float) this.mGoToFullShadeAppearingTranslation));
            this.mAnimationProperties.duration = ((long) (((float) Math.pow((double) i, 0.699999988079071d)) * 100.0f)) + 350;
        }
    }

    private boolean applyWithoutAnimation(ExpandableView expandableView, ExpandableViewState expandableViewState) {
        if (this.mShadeExpanded || ViewState.isAnimatingY(expandableView) || this.mHeadsUpDisappearChildren.contains(expandableView) || this.mHeadsUpAppearChildren.contains(expandableView) || NotificationStackScrollLayout.isPinnedHeadsUp(expandableView) || applyWithAnimation(expandableView)) {
            return false;
        }
        expandableViewState.applyToView(expandableView);
        return true;
    }

    /* access modifiers changed from: protected */
    public long calculateChildAnimationDelay(ExpandableViewState expandableViewState, int i) {
        ExpandableView expandableView;
        AnimationFilter animationFilter = this.mAnimationFilter;
        if (animationFilter.hasGoToFullShadeEvent) {
            return calculateDelayGoToFullShade(expandableViewState, i);
        }
        long j = animationFilter.customDelay;
        if (j != -1) {
            return j;
        }
        long j2 = 0;
        Iterator<NotificationStackScrollLayout.AnimationEvent> it = this.mNewEvents.iterator();
        while (it.hasNext()) {
            NotificationStackScrollLayout.AnimationEvent next = it.next();
            long j3 = 80;
            int i2 = next.animationType;
            if (i2 != 0) {
                if (i2 != 1) {
                    if (i2 == 2) {
                        j3 = 32;
                    }
                }
                int i3 = expandableViewState.notGoneIndex;
                if (next.viewAfterChangingView == null) {
                    expandableView = this.mHostLayout.getLastChildNotGone();
                } else {
                    expandableView = (ExpandableView) next.viewAfterChangingView;
                }
                if (expandableView != null) {
                    int i4 = expandableView.getViewState().notGoneIndex;
                    if (i3 >= i4) {
                        i3++;
                    }
                    j2 = Math.max(((long) Math.max(0, Math.min(2, Math.abs(i3 - i4) - 1))) * j3, j2);
                }
            } else {
                j2 = Math.max(((long) (2 - Math.max(0, Math.min(2, Math.abs(expandableViewState.notGoneIndex - next.mChangingView.getViewState().notGoneIndex) - 1)))) * 80, j2);
            }
        }
        return j2;
    }

    private long calculateDelayGoToFullShade(ExpandableViewState expandableViewState, int i) {
        int notGoneIndex = this.mShelf.getNotGoneIndex();
        float f = (float) expandableViewState.notGoneIndex;
        float f2 = (float) notGoneIndex;
        long j = 0;
        if (f > f2) {
            j = 0 + ((long) (((double) (((float) Math.pow((double) i, 0.699999988079071d)) * 48.0f)) * 0.25d));
            f = f2;
        }
        return j + ((long) (((float) Math.pow((double) f, 0.699999988079071d)) * 48.0f));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AnimatorListenerAdapter getGlobalAnimationFinishedListener() {
        if (!this.mAnimationListenerPool.empty()) {
            return this.mAnimationListenerPool.pop();
        }
        return new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.notification.stack.StackStateAnimator.AnonymousClass2 */
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
    /* access modifiers changed from: public */
    private void onAnimationFinished() {
        this.mHostLayout.onChildAnimationFinished();
        Iterator<ExpandableView> it = this.mTransientViewsToRemove.iterator();
        while (it.hasNext()) {
            ExpandableView next = it.next();
            next.getTransientContainer().removeTransientView(next);
        }
        this.mTransientViewsToRemove.clear();
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x01a9  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processAnimationEvents(java.util.ArrayList<com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnimationEvent> r15) {
        /*
        // Method dump skipped, instructions count: 464
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.StackStateAnimator.processAnimationEvents(java.util.ArrayList):void");
    }

    public static void removeTransientView(ExpandableView expandableView) {
        if (expandableView.getTransientContainer() != null) {
            expandableView.getTransientContainer().removeTransientView(expandableView);
        }
    }

    public void animateOverScrollToAmount(float f, final boolean z, final boolean z2) {
        float currentOverScrollAmount = this.mHostLayout.getCurrentOverScrollAmount(z);
        if (f != currentOverScrollAmount) {
            cancelOverScrollAnimators(z);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(currentOverScrollAmount, f);
            ofFloat.setDuration(300L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.statusbar.notification.stack.StackStateAnimator.AnonymousClass3 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    StackStateAnimator.this.mHostLayout.setOverScrollAmount(((Float) valueAnimator.getAnimatedValue()).floatValue(), z, false, false, z2);
                }
            });
            ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofFloat.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.statusbar.notification.stack.StackStateAnimator.AnonymousClass4 */

                public void onAnimationEnd(Animator animator) {
                    if (z) {
                        StackStateAnimator.this.mTopOverScrollAnimator = null;
                    } else {
                        StackStateAnimator.this.mBottomOverScrollAnimator = null;
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
