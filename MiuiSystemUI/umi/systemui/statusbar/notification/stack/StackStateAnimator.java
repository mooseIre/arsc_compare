package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0009R$dimen;
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
    /* access modifiers changed from: private */
    public Stack<AnimatorListenerAdapter> mAnimationListenerPool = new Stack<>();
    protected final AnimationProperties mAnimationProperties;
    /* access modifiers changed from: private */
    public HashSet<Animator> mAnimatorSet = new HashSet<>();
    /* access modifiers changed from: private */
    public ValueAnimator mBottomOverScrollAnimator;
    private long mCurrentAdditionalDelay;
    private long mCurrentLength;
    private final int mGoToFullShadeAppearingTranslation;
    /* access modifiers changed from: private */
    public HashSet<View> mHeadsUpAppearChildren = new HashSet<>();
    private int mHeadsUpAppearHeightBottom;
    private HashSet<View> mHeadsUpDisappearChildren = new HashSet<>();
    public NotificationStackScrollLayout mHostLayout;
    /* access modifiers changed from: private */
    public ArrayList<View> mNewAddChildren = new ArrayList<>();
    private ArrayList<NotificationStackScrollLayout.AnimationEvent> mNewEvents = new ArrayList<>();
    private boolean mShadeExpanded;
    private NotificationShelf mShelf;
    private int[] mTmpLocation = new int[2];
    private final ExpandableViewState mTmpState = new ExpandableViewState();
    /* access modifiers changed from: private */
    public ValueAnimator mTopOverScrollAnimator;
    protected ArrayList<ExpandableView> mTransientViewsToRemove = new ArrayList<>();

    /* access modifiers changed from: protected */
    public abstract boolean applyWithAnimation(View view);

    /* access modifiers changed from: protected */
    public Interpolator getCustomInterpolator(View view, Property property) {
        return null;
    }

    public StackStateAnimator(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mHostLayout = notificationStackScrollLayout;
        this.mGoToFullShadeAppearingTranslation = notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(C0009R$dimen.go_to_full_shade_appearing_translation);
        notificationStackScrollLayout.getContext().getResources().getDimensionPixelSize(C0009R$dimen.pulsing_notification_appear_translation);
        this.mAnimationProperties = new AnimationProperties() {
            public AnimationFilter getAnimationFilter() {
                return StackStateAnimator.this.mAnimationFilter;
            }

            public AnimatorListenerAdapter getAnimationFinishListener(Property property) {
                return StackStateAnimator.this.getGlobalAnimationFinishedListener();
            }

            public boolean wasAdded(View view) {
                return StackStateAnimator.this.mNewAddChildren.contains(view);
            }

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
            this.mAnimationProperties.duration = ((long) (((float) Math.pow((double) i, 0.699999988079071d)) * 100.0f)) + 514;
        }
    }

    private boolean applyWithoutAnimation(ExpandableView expandableView, ExpandableViewState expandableViewState) {
        if (this.mShadeExpanded || ViewState.isAnimatingY(expandableView) || this.mHeadsUpDisappearChildren.contains(expandableView) || this.mHeadsUpAppearChildren.contains(expandableView) || NotificationStackScrollLayout.isPinnedHeadsUp(expandableView) || applyWithAnimation(expandableView)) {
            return false;
        }
        expandableViewState.applyToView(expandableView);
        return true;
    }

    private long calculateChildAnimationDelay(ExpandableViewState expandableViewState, int i) {
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
            r14 = this;
            java.util.Iterator r15 = r15.iterator()
        L_0x0004:
            boolean r0 = r15.hasNext()
            if (r0 == 0) goto L_0x01cf
            java.lang.Object r0 = r15.next()
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent r0 = (com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnimationEvent) r0
            com.android.systemui.statusbar.notification.row.ExpandableView r7 = r0.mChangingView
            int r1 = r0.animationType
            if (r1 != 0) goto L_0x002b
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r7.getViewState()
            if (r1 == 0) goto L_0x0004
            boolean r2 = r1.gone
            if (r2 == 0) goto L_0x0021
            goto L_0x0004
        L_0x0021:
            r1.applyToView(r7)
            java.util.ArrayList<android.view.View> r1 = r14.mNewAddChildren
            r1.add(r7)
            goto L_0x01c8
        L_0x002b:
            r2 = 1
            if (r1 != r2) goto L_0x00a1
            int r1 = r7.getVisibility()
            if (r1 == 0) goto L_0x0038
            removeTransientView(r7)
            goto L_0x0004
        L_0x0038:
            android.view.View r1 = r0.viewAfterChangingView
            r2 = -1082130432(0xffffffffbf800000, float:-1.0)
            if (r1 == 0) goto L_0x008a
            float r1 = r7.getTranslationY()
            boolean r3 = r7 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r3 == 0) goto L_0x0067
            android.view.View r3 = r0.viewAfterChangingView
            boolean r4 = r3 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r4 == 0) goto L_0x0067
            r4 = r7
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r4
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r3
            boolean r5 = r4.isRemoved()
            if (r5 == 0) goto L_0x0067
            boolean r5 = r4.wasChildInGroupWhenRemoved()
            if (r5 == 0) goto L_0x0067
            boolean r3 = r3.isChildInGroup()
            if (r3 != 0) goto L_0x0067
            float r1 = r4.getTranslationWhenRemoved()
        L_0x0067:
            int r3 = r7.getActualHeight()
            android.view.View r4 = r0.viewAfterChangingView
            com.android.systemui.statusbar.notification.row.ExpandableView r4 = (com.android.systemui.statusbar.notification.row.ExpandableView) r4
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r4 = r4.getViewState()
            float r4 = r4.yTranslation
            float r3 = (float) r3
            r5 = 1073741824(0x40000000, float:2.0)
            float r6 = r3 / r5
            float r1 = r1 + r6
            float r4 = r4 - r1
            float r4 = r4 * r5
            float r4 = r4 / r3
            r1 = 1065353216(0x3f800000, float:1.0)
            float r1 = java.lang.Math.min(r4, r1)
            float r1 = java.lang.Math.max(r1, r2)
            r6 = r1
            goto L_0x008b
        L_0x008a:
            r6 = r2
        L_0x008b:
            r2 = 464(0x1d0, double:2.29E-321)
            r4 = 0
            r8 = 0
            r9 = 0
            com.android.systemui.statusbar.notification.stack.-$$Lambda$StackStateAnimator$TZG1mUHYcGvJktxtVi9se9juSC8 r10 = new com.android.systemui.statusbar.notification.stack.-$$Lambda$StackStateAnimator$TZG1mUHYcGvJktxtVi9se9juSC8
            r10.<init>()
            r11 = 0
            r1 = r7
            r7 = r8
            r8 = r9
            r9 = r10
            r10 = r11
            r1.performRemoveAnimation(r2, r4, r6, r7, r8, r9, r10)
            goto L_0x01c8
        L_0x00a1:
            r3 = 2
            if (r1 != r3) goto L_0x00c4
            float r1 = r7.getTranslation()
            float r1 = java.lang.Math.abs(r1)
            int r2 = r7.getWidth()
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 != 0) goto L_0x01c8
            android.view.ViewGroup r1 = r7.getTransientContainer()
            if (r1 == 0) goto L_0x01c8
            android.view.ViewGroup r1 = r7.getTransientContainer()
            r1.removeTransientView(r7)
            goto L_0x01c8
        L_0x00c4:
            r3 = 10
            if (r1 != r3) goto L_0x00cf
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r7 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r7
            r7.prepareExpansionChanged()
            goto L_0x01c8
        L_0x00cf:
            r3 = 11
            r4 = 0
            if (r1 != r3) goto L_0x0103
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r7.getViewState()
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r2 = r14.mTmpState
            r2.copyFrom(r1)
            boolean r1 = r0.headsUpFromBottom
            if (r1 == 0) goto L_0x00e9
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r14.mTmpState
            int r2 = r14.mHeadsUpAppearHeightBottom
            float r2 = (float) r2
            r1.yTranslation = r2
            goto L_0x00f7
        L_0x00e9:
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r14.mTmpState
            r1.yTranslation = r4
            r2 = 0
            int r1 = ANIMATION_DURATION_HEADS_UP_APPEAR_CLOSED
            long r4 = (long) r1
            r6 = 1
            r1 = r7
            r1.performAddAnimation(r2, r4, r6)
        L_0x00f7:
            java.util.HashSet<android.view.View> r1 = r14.mHeadsUpAppearChildren
            r1.add(r7)
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r14.mTmpState
            r1.applyToView(r7)
            goto L_0x01c8
        L_0x0103:
            r3 = 12
            r5 = 13
            if (r1 == r3) goto L_0x010b
            if (r1 != r5) goto L_0x01c8
        L_0x010b:
            java.util.HashSet<android.view.View> r1 = r14.mHeadsUpDisappearChildren
            r1.add(r7)
            r1 = 0
            int r3 = r0.animationType
            r6 = 0
            if (r3 != r5) goto L_0x0119
            r3 = 120(0x78, float:1.68E-43)
            goto L_0x011a
        L_0x0119:
            r3 = r6
        L_0x011a:
            android.view.ViewParent r5 = r7.getParent()
            if (r5 != 0) goto L_0x014a
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r1 = r14.mHostLayout
            r1.addTransientView(r7, r6)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r1 = r14.mHostLayout
            r7.setTransientContainer(r1)
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r14.mTmpState
            r1.initFrom(r7)
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r14.mTmpState
            r1.yTranslation = r4
            com.android.systemui.statusbar.notification.stack.AnimationFilter r5 = r14.mAnimationFilter
            r5.animateY = r2
            com.android.systemui.statusbar.notification.stack.AnimationProperties r5 = r14.mAnimationProperties
            int r8 = r3 + 120
            long r8 = (long) r8
            r5.delay = r8
            r8 = 300(0x12c, double:1.48E-321)
            r5.duration = r8
            r1.animateTo(r7, r5)
            com.android.systemui.statusbar.notification.stack.-$$Lambda$StackStateAnimator$_Pk5aD8YGtEkv3ND7OecxMpqHJ4 r1 = new com.android.systemui.statusbar.notification.stack.-$$Lambda$StackStateAnimator$_Pk5aD8YGtEkv3ND7OecxMpqHJ4
            r1.<init>()
        L_0x014a:
            r9 = r1
            boolean r1 = r7 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r1 == 0) goto L_0x01a6
            r1 = r7
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r1 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r1
            boolean r5 = r1.isDismissed()
            r2 = r2 ^ r5
            com.android.systemui.statusbar.notification.collection.NotificationEntry r1 = r1.getEntry()
            com.android.systemui.statusbar.notification.icon.IconPack r5 = r1.getIcons()
            com.android.systemui.statusbar.StatusBarIconView r5 = r5.getStatusBarIcon()
            com.android.systemui.statusbar.notification.icon.IconPack r1 = r1.getIcons()
            com.android.systemui.statusbar.StatusBarIconView r1 = r1.getCenteredIcon()
            if (r1 == 0) goto L_0x0174
            android.view.ViewParent r8 = r1.getParent()
            if (r8 == 0) goto L_0x0174
            r5 = r1
        L_0x0174:
            android.view.ViewParent r1 = r5.getParent()
            if (r1 == 0) goto L_0x01a6
            int[] r1 = r14.mTmpLocation
            r5.getLocationOnScreen(r1)
            int[] r1 = r14.mTmpLocation
            r1 = r1[r6]
            float r1 = (float) r1
            float r4 = r5.getTranslationX()
            float r1 = r1 - r4
            float r4 = com.android.systemui.statusbar.notification.stack.ViewState.getFinalTranslationX(r5)
            float r1 = r1 + r4
            int r4 = r5.getWidth()
            float r4 = (float) r4
            r5 = 1048576000(0x3e800000, float:0.25)
            float r4 = r4 * r5
            float r1 = r1 + r4
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r14.mHostLayout
            int[] r5 = r14.mTmpLocation
            r4.getLocationOnScreen(r5)
            int[] r4 = r14.mTmpLocation
            r4 = r4[r6]
            float r4 = (float) r4
            float r1 = r1 - r4
            r8 = r1
            goto L_0x01a7
        L_0x01a6:
            r8 = r4
        L_0x01a7:
            if (r2 == 0) goto L_0x01c3
            r4 = 420(0x1a4, double:2.075E-321)
            long r10 = (long) r3
            r6 = 0
            r12 = 1
            android.animation.AnimatorListenerAdapter r13 = r14.getGlobalAnimationFinishedListener()
            r1 = r7
            r2 = r4
            r4 = r10
            r7 = r12
            r10 = r13
            long r1 = r1.performRemoveAnimation(r2, r4, r6, r7, r8, r9, r10)
            com.android.systemui.statusbar.notification.stack.AnimationProperties r3 = r14.mAnimationProperties
            long r4 = r3.delay
            long r4 = r4 + r1
            r3.delay = r4
            goto L_0x01c8
        L_0x01c3:
            if (r9 == 0) goto L_0x01c8
            r9.run()
        L_0x01c8:
            java.util.ArrayList<com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent> r1 = r14.mNewEvents
            r1.add(r0)
            goto L_0x0004
        L_0x01cf:
            return
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
