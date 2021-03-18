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

    /* JADX WARN: Incorrect args count in method signature: ()V */
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$createTouchHandler$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super();
        this.this$0 = miuiNotificationPanelViewController;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController.NotificationPanelTouchHandler
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
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController.NotificationPanelTouchHandler
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
            KeyguardPanelViewInjector keyguardPanelViewInjector = this.this$0.mKeyguardPanelViewInjector;
            int i = this.this$0.mBarState;
            float f = this.mInitialTouchX;
            float f2 = this.mInitialTouchY;
            MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.this$0;
            z = keyguardPanelViewInjector.onTouchEvent(motionEvent, i, f, f2, miuiNotificationPanelViewController2.mIsExpanding, miuiNotificationPanelViewController2.mHintAnimationRunning, miuiNotificationPanelViewController2.mQsExpanded, miuiNotificationPanelViewController2.mDozing, miuiNotificationPanelViewController2.mTracking, miuiNotificationPanelViewController2.mClosing);
        }
        if (handleMiniWindowTracking(motionEvent)) {
            return true;
        }
        if (this.this$0.isOnKeyguard()) {
            resetPanelTouchState();
        } else if ((this.isFullyCollapsedOnDown || this.isFullyExpandedOnDown) && handlePanelTouch(motionEvent)) {
            return true;
        }
        return z;
    }

    private final boolean handleMiniWindowTracking(MotionEvent motionEvent) {
        boolean z = this.this$0.mTrackingMiniWindowHeadsUp;
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
        miuiNotificationPanelViewController.mTrackingMiniWindowHeadsUp = miuiNotificationPanelViewController.isTrackingMiniWindowHeadsUp();
        int actionMasked = motionEvent.getActionMasked();
        return (actionMasked == 1 || actionMasked == 3) ? z : this.this$0.mTrackingMiniWindowHeadsUp;
    }

    private final boolean handleStretchState(float f, float f2, float f3) {
        if (Math.abs(f) > Math.abs(f2) && f > ((float) this.this$0.mTouchSlop)) {
            Object obj = this.this$0.controlPanelController.get();
            Intrinsics.checkExpressionValueIsNotNull(obj, "controlPanelController.get()");
            boolean isUseControlCenter = ((ControlPanelController) obj).isUseControlCenter();
            boolean z = this.isFullyCollapsedOnDown && !(this.this$0.mExpandingFromHeadsUp) && !(this.this$0.mNssCoveredQs);
            boolean z2 = this.isFullyExpandedOnDown && isUseControlCenter && this.this$0.getMNotificationStackScroller().isScrolledToTop();
            if (z || z2) {
                if (isUseControlCenter && !(this.this$0.getMPanelStretching())) {
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
        // Method dump skipped, instructions count: 121
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
                    if (!(this.this$0.mNssCoveringQs)) {
                        this.this$0.mNssCoveringQs = handleSlideState(f2, f, y);
                    }
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
                    if ((miuiNotificationPanelViewController.getMPanelStretching()) || (this.this$0.mPanelCollapsing) || (this.this$0.mNssCoveringQs)) {
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
            this.this$0.setMPanelOpening(false);
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
                    if (!(this.this$0.mPanelIntercepting)) {
                        this.this$0.mPanelIntercepting = handleStretchState || handleCollapseState || handleSlideState;
                    }
                    if (this.isFullyCollapsedOnDown) {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
                        miuiNotificationPanelViewController.setMPanelOpening(!(miuiNotificationPanelViewController.mPanelOpening) ? handleStretchState : true);
                    } else if (this.isFullyExpandedOnDown) {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.this$0;
                        if (miuiNotificationPanelViewController2.mNssCoveringQs) {
                            handleSlideState = true;
                        }
                        miuiNotificationPanelViewController2.mNssCoveringQs = handleSlideState;
                    }
                    if (!(this.this$0.mPanelIntercepting) || (this.this$0.mNssCoveringQs) || (this.this$0.mNssCoveredQs)) {
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
                        ValueAnimator valueAnimator = this.this$0.mQsTopPaddingAnimator;
                        if (valueAnimator != null && valueAnimator.isRunning()) {
                            valueAnimator.cancel();
                        }
                        if (this.this$0.mVelocityTracker == null) {
                            this.this$0.initVelocityTracker();
                        }
                        this.this$0.trackMovement(motionEvent);
                        this.this$0.handleNssCoverQs(y - this.mLastTouchY);
                    } else if ((this.this$0.getMPanelStretching()) || (this.this$0.mPanelCollapsing)) {
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
            this.this$0.setMPanelOpening(false);
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelCollapsing = false;
            this.this$0.mPanelIntercepting = false;
            this.this$0.setMStretchLength(0.0f);
            if (!(this.this$0.getMPanelAppeared()) && !(this.this$0.mExpandingFromHeadsUp)) {
                this.this$0.scheduleHidePanel();
            }
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mLastTouchY = y;
            this.this$0.setMPanelOpening(false);
            this.this$0.setMPanelStretching(false);
            this.this$0.mPanelCollapsing = false;
            this.this$0.mPanelIntercepting = false;
        }
        return this.this$0.mPanelIntercepting;
    }

    public final void resetPanelTouchState() {
        this.this$0.setMPanelOpening(false);
        this.this$0.setMPanelStretching(false);
        this.this$0.mPanelCollapsing = false;
        this.this$0.mPanelIntercepting = false;
        this.this$0.setMStretchLength(0.0f);
    }
}
