package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import android.view.MotionEvent;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$createTouchHandler$1 extends NotificationPanelViewController.NotificationPanelTouchHandler {
    private boolean isFullyCollapsedOnDown;
    private boolean isFullyExpandedOnDown;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchY;
    private int mTrackingPointer;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$createTouchHandler$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super();
        this.this$0 = miuiNotificationPanelViewController;
    }

    /* access modifiers changed from: protected */
    public boolean onMiuiIntercept(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (motionEvent.getActionMasked() == 0) {
            this.isFullyCollapsedOnDown = this.this$0.isFullyCollapsed();
            this.isFullyExpandedOnDown = this.this$0.isFullyExpanded();
            this.this$0.cancelFlingSpring();
        }
        if (!this.this$0.isOnKeyguard() && (((ModalController) Dependency.get(ModalController.class)).maybeDispatchMotionEvent(motionEvent) || onPanelIntercept(motionEvent))) {
            return true;
        }
        if (this.this$0.isOnKeyguard()) {
            return this.this$0.mKeyguardPanelViewInjector.onInterceptTouchEvent(motionEvent);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleMiuiTouch(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (motionEvent.getActionMasked() == 0) {
            this.isFullyCollapsedOnDown = this.this$0.isFullyCollapsed();
            this.isFullyExpandedOnDown = this.this$0.isFullyExpanded();
            this.this$0.cancelFlingSpring();
        }
        if (((ModalController) Dependency.get(ModalController.class)).maybeDispatchMotionEvent(motionEvent)) {
            return true;
        }
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
        boolean z = false;
        if (miuiNotificationPanelViewController.mClosing) {
            return false;
        }
        if (miuiNotificationPanelViewController.isOnKeyguard()) {
            KeyguardPanelViewInjector access$getMKeyguardPanelViewInjector$p = this.this$0.mKeyguardPanelViewInjector;
            int access$getMBarState$p = this.this$0.mBarState;
            float f = this.mInitialTouchX;
            float f2 = this.mInitialTouchY;
            MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.this$0;
            z = access$getMKeyguardPanelViewInjector$p.onTouchEvent(motionEvent, access$getMBarState$p, f, f2, miuiNotificationPanelViewController2.mIsExpanding, miuiNotificationPanelViewController2.mHintAnimationRunning, miuiNotificationPanelViewController2.mQsExpanded, miuiNotificationPanelViewController2.mDozing, miuiNotificationPanelViewController2.mTracking, miuiNotificationPanelViewController2.mClosing);
        }
        if (handleMiniWindowTracking(motionEvent)) {
            return true;
        }
        if (this.this$0.isOnKeyguard() || ((!this.isFullyCollapsedOnDown && !this.isFullyExpandedOnDown) || !handlePanelTouch(motionEvent))) {
            return z;
        }
        return true;
    }

    private final boolean handleMiniWindowTracking(MotionEvent motionEvent) {
        boolean access$getMTrackingMiniWindowHeadsUp$p = this.this$0.mTrackingMiniWindowHeadsUp;
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
        miuiNotificationPanelViewController.mTrackingMiniWindowHeadsUp = miuiNotificationPanelViewController.isTrackingMiniWindowHeadsUp();
        int actionMasked = motionEvent.getActionMasked();
        return (actionMasked == 1 || actionMasked == 3) ? access$getMTrackingMiniWindowHeadsUp$p : this.this$0.mTrackingMiniWindowHeadsUp;
    }

    private final boolean handleStretchState(float f, float f2, float f3) {
        if (Math.abs(f) > Math.abs(f2) && f > ((float) this.this$0.mTouchSlop)) {
            Object obj = this.this$0.controlPanelController.get();
            Intrinsics.checkExpressionValueIsNotNull(obj, "controlPanelController.get()");
            boolean isUseControlCenter = ((ControlPanelController) obj).isUseControlCenter();
            boolean z = this.isFullyCollapsedOnDown && !this.this$0.mExpandingFromHeadsUp && !this.this$0.mNssCoveredQs;
            boolean z2 = this.isFullyExpandedOnDown && isUseControlCenter && this.this$0.getMNotificationStackScroller().isScrolledToTop();
            if (z || z2) {
                if (isUseControlCenter && !this.this$0.getMPanelStretching()) {
                    this.this$0.cancelFlingSpring();
                }
                return true;
            }
        }
        return false;
    }

    private final boolean handleCollapseState(float f, float f2, float f3) {
        if (!this.isFullyExpandedOnDown || Math.abs(f) <= Math.abs(f2) || f >= ((float) 0) || Math.abs(f) <= ((float) this.this$0.mTouchSlop)) {
            return false;
        }
        if (f3 > (((float) this.this$0.getPanelView().getHeight()) - this.this$0.mBottomAreaCollapseHotZone) - ((float) this.this$0.getNotificationContainerParent().getPaddingBottom()) || (!this.this$0.isQsExpanded() && this.this$0.getMNotificationStackScroller().isScrolledToBottom())) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0078 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean handleSlideState(float r3, float r4, float r5) {
        /*
            r2 = this;
            boolean r5 = r2.isFullyExpandedOnDown
            r0 = 0
            if (r5 == 0) goto L_0x0078
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r5 = r2.this$0
            dagger.Lazy r5 = r5.controlPanelController
            java.lang.Object r5 = r5.get()
            java.lang.String r1 = "controlPanelController.get()"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r1)
            com.android.systemui.controlcenter.phone.ControlPanelController r5 = (com.android.systemui.controlcenter.phone.ControlPanelController) r5
            boolean r5 = r5.isUseControlCenter()
            if (r5 != 0) goto L_0x0078
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r5 = r2.this$0
            boolean r5 = r5.isQsExpanded()
            if (r5 != 0) goto L_0x0078
            float r5 = java.lang.Math.abs(r3)
            float r4 = java.lang.Math.abs(r4)
            int r4 = (r5 > r4 ? 1 : (r5 == r4 ? 0 : -1))
            if (r4 <= 0) goto L_0x0078
            float r4 = java.lang.Math.abs(r3)
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r5 = r2.this$0
            int r5 = r5.mTouchSlop
            float r5 = (float) r5
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x0078
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r4 = r2.this$0
            boolean r4 = r4.mNssCoveredQs
            r5 = 1
            if (r4 == 0) goto L_0x005a
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r2 = r2.this$0
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r2 = r2.getMNotificationStackScroller()
            boolean r2 = r2.isScrolledToTop()
            if (r2 == 0) goto L_0x0078
            float r2 = (float) r0
            int r2 = (r3 > r2 ? 1 : (r3 == r2 ? 0 : -1))
            if (r2 <= 0) goto L_0x0078
            return r5
        L_0x005a:
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r4 = r2.this$0
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r4.getMNotificationStackScroller()
            boolean r4 = r4.isScrolledToTop()
            if (r4 == 0) goto L_0x0078
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r2 = r2.this$0
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r2 = r2.getMNotificationStackScroller()
            boolean r2 = r2.isScrolledToBottom()
            if (r2 != 0) goto L_0x0078
            float r2 = (float) r0
            int r2 = (r3 > r2 ? 1 : (r3 == r2 ? 0 : -1))
            if (r2 >= 0) goto L_0x0078
            return r5
        L_0x0078:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$createTouchHandler$1.handleSlideState(float, float, float):boolean");
    }

    private final void handlePointerUp(MotionEvent motionEvent) {
        int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
        if (this.mTrackingPointer == pointerId) {
            int i = 0;
            if (motionEvent.getPointerId(0) == pointerId) {
                i = 1;
            }
            this.mTrackingPointer = motionEvent.getPointerId(i);
            this.mInitialTouchX = motionEvent.getX(i);
            this.mInitialTouchY = motionEvent.getY(i);
        }
    }

    private final void handleStretchFromHeadsUp(float f, float f2) {
        if (this.this$0.mStretchFromHeadsUpRequested) {
            this.mInitialTouchX = f;
            this.mInitialTouchY = f2;
            this.this$0.mStretchFromHeadsUpRequested = false;
        }
    }

    public final boolean onPanelIntercept(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        boolean z = false;
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = x - this.mInitialTouchX;
                    float f2 = y - this.mInitialTouchY;
                    this.this$0.setMPanelStretching(handleStretchState(f2, f, y));
                    this.this$0.mPanelCollapsing = handleCollapseState(f2, f, this.mInitialTouchY);
                    if (!this.this$0.mNssCoveringQs) {
                        this.this$0.mNssCoveringQs = handleSlideState(f2, f, y);
                    }
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
                    if (miuiNotificationPanelViewController.getMPanelStretching() || this.this$0.mPanelCollapsing || this.this$0.mNssCoveringQs) {
                        z = true;
                    }
                    miuiNotificationPanelViewController.mPanelIntercepting = z;
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        handlePointerUp(motionEvent);
                    }
                }
            }
            this.this$0.mPanelCollapsing = false;
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelIntercepting = false;
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.this$0.mPanelOpening = false;
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelCollapsing = false;
            this.this$0.mPanelIntercepting = false;
        }
        this.mLastTouchY = y;
        return this.this$0.mPanelIntercepting;
    }

    public final boolean handlePanelTouch(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        boolean z = false;
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    handleStretchFromHeadsUp(x, y);
                    float f = x - this.mInitialTouchX;
                    float f2 = y - this.mInitialTouchY;
                    boolean handleStretchState = handleStretchState(f2, f, y);
                    boolean handleCollapseState = handleCollapseState(f2, f, this.mInitialTouchY);
                    boolean handleSlideState = handleSlideState(f2, f, y);
                    if (!this.this$0.mPanelIntercepting) {
                        this.this$0.mPanelIntercepting = handleStretchState || handleCollapseState || handleSlideState;
                    }
                    if (this.isFullyCollapsedOnDown) {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
                        miuiNotificationPanelViewController.mPanelOpening = !miuiNotificationPanelViewController.mPanelOpening ? handleStretchState : true;
                    } else if (this.isFullyExpandedOnDown) {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.this$0;
                        if (miuiNotificationPanelViewController2.mNssCoveringQs) {
                            handleSlideState = true;
                        }
                        miuiNotificationPanelViewController2.mNssCoveringQs = handleSlideState;
                    }
                    if (!this.this$0.mPanelIntercepting || this.this$0.mNssCoveringQs || this.this$0.mNssCoveredQs) {
                        this.this$0.setMPanelStretching(handleStretchState);
                        this.this$0.mPanelCollapsing = handleCollapseState;
                    } else {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController3 = this.this$0;
                        if (f2 > ((float) 0)) {
                            z = true;
                        }
                        miuiNotificationPanelViewController3.setMPanelStretching(z);
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController4 = this.this$0;
                        miuiNotificationPanelViewController4.mPanelCollapsing = !miuiNotificationPanelViewController4.getMPanelStretching();
                    }
                    if (this.this$0.mNssCoveringQs) {
                        ValueAnimator access$getMQsTopPaddingAnimator$p = this.this$0.mQsTopPaddingAnimator;
                        if (access$getMQsTopPaddingAnimator$p != null && access$getMQsTopPaddingAnimator$p.isRunning()) {
                            access$getMQsTopPaddingAnimator$p.cancel();
                        }
                        if (this.this$0.mVelocityTracker == null) {
                            this.this$0.initVelocityTracker();
                        }
                        this.this$0.trackMovement(motionEvent);
                        this.this$0.handleNssCoverQs(y - this.mLastTouchY);
                    } else if (this.this$0.getMPanelStretching() || this.this$0.mPanelCollapsing) {
                        this.this$0.setMStretchLength(f2);
                    }
                    this.mLastTouchY = y;
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        handlePointerUp(motionEvent);
                    }
                }
            }
            if (this.this$0.mNssCoveringQs) {
                this.this$0.trackMovement(motionEvent);
                MiuiNotificationPanelViewController miuiNotificationPanelViewController5 = this.this$0;
                miuiNotificationPanelViewController5.endNssCoveringQsMotion(miuiNotificationPanelViewController5.getCurrentQSVelocity());
                this.this$0.recycleVelocityTracker();
            }
            this.this$0.mPanelOpening = false;
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelCollapsing = false;
            this.this$0.mPanelIntercepting = false;
            this.this$0.setMStretchLength(0.0f);
            if (!this.this$0.getMPanelAppeared() && !this.this$0.mExpandingFromHeadsUp) {
                this.this$0.scheduleHidePanel();
            }
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mLastTouchY = y;
            this.this$0.mPanelOpening = false;
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelCollapsing = false;
            this.this$0.mPanelIntercepting = false;
        }
        return this.this$0.mPanelIntercepting;
    }
}
