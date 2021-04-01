package com.android.systemui.statusbar.notification.policy;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Looper;
import android.util.MiuiMultiWindowUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventReason;
import com.miui.systemui.events.MiniWindowEventSource;
import com.miui.systemui.events.MiniWindowEvents;
import com.miui.systemui.util.HapticFeedBackImpl;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper {
    private boolean mAbandonMiniWindowTracks;
    private final Context mContext;
    private boolean mEnterAnimationRunning;
    private final EventTracker mEventTracker;
    private final MiniWindowExpandParameters mExpandedParams;
    private final AppMiniWindowRowTouchHelper$mHandler$1 mHandler;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mMaxTriggerThreshold;
    private final NotificationEntryManager mNotificationEntryManager;
    private int mPickedChildHeight;
    private int mPickedChildLeft;
    private int mPickedChildRight;
    private int mPickedChildTop;
    private int mPickedChildWidth;
    private MiuiExpandableNotificationRow mPickedMiniWindowChild;
    private final AppMiniWindowRowTouchCallback mTouchCallback;
    private final int mTouchSlop;
    private boolean mTouchingMiniWindowRow;
    private boolean mTrackingMiniWindowRow;
    private int mTrackingPointer;
    private float mTriggerThreshold;
    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private final MiniWindowEventSource source;

    public AppMiniWindowRowTouchHelper(@NotNull AppMiniWindowRowTouchCallback appMiniWindowRowTouchCallback, @NotNull NotificationEntryManager notificationEntryManager, @NotNull EventTracker eventTracker, @NotNull MiniWindowEventSource miniWindowEventSource) {
        Intrinsics.checkParameterIsNotNull(appMiniWindowRowTouchCallback, "touchCallback");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(eventTracker, "eventTracker");
        Intrinsics.checkParameterIsNotNull(miniWindowEventSource, "source");
        this.source = miniWindowEventSource;
        this.mContext = appMiniWindowRowTouchCallback.getContext();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(appMiniWindowRowTouchCallback.getContext());
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(touchCallback.getContext())");
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mTouchCallback = appMiniWindowRowTouchCallback;
        this.mEventTracker = eventTracker;
        this.mNotificationEntryManager = notificationEntryManager;
        this.mExpandedParams = new MiniWindowExpandParameters();
        this.mHandler = new AppMiniWindowRowTouchHelper$mHandler$1(this, Looper.getMainLooper());
    }

    public final boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if ((!this.mTouchingMiniWindowRow || this.mAbandonMiniWindowTracks) && motionEvent.getActionMasked() == 2) {
            return false;
        }
        int validPointerIndex = getValidPointerIndex(motionEvent);
        float x = motionEvent.getX(validPointerIndex);
        float y = motionEvent.getY(validPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    if (this.mTouchingMiniWindowRow && f < ((float) (-this.mTouchSlop))) {
                        this.mAbandonMiniWindowTracks = true;
                    }
                    if (this.mTouchingMiniWindowRow && !this.mTrackingMiniWindowRow && f > ((float) this.mTouchSlop) && Math.abs(f) > Math.abs(x - this.mInitialTouchX)) {
                        this.mInitialTouchX = x;
                        this.mInitialTouchY = y;
                        onMiniWindowTrackingStart();
                        onMiniWindowTracking(0.0f);
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        handlePointerUp(motionEvent);
                    }
                }
            }
            onMiniWindowTrackingEnd();
        } else {
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            ExpandableView childAtRawPosition = this.mTouchCallback.getChildAtRawPosition(motionEvent.getRawX(validPointerIndex), motionEvent.getRawY(validPointerIndex));
            if (childAtRawPosition instanceof MiuiExpandableNotificationRow) {
                MiuiExpandableNotificationRow miuiExpandableNotificationRow = (MiuiExpandableNotificationRow) childAtRawPosition;
                if (miuiExpandableNotificationRow.canSlideToMiniWindow()) {
                    boolean canChildBePicked = this.mTouchCallback.canChildBePicked(childAtRawPosition);
                    this.mTouchingMiniWindowRow = canChildBePicked;
                    if (canChildBePicked) {
                        onMiniWindowChildPicked(miuiExpandableNotificationRow);
                    }
                }
            }
        }
        return this.mTrackingMiniWindowRow;
    }

    public final boolean onTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        this.mVelocityTracker.addMovement(motionEvent);
        if ((!this.mTrackingMiniWindowRow || this.mAbandonMiniWindowTracks) && motionEvent.getActionMasked() == 2) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                float y = motionEvent.getY(getValidPointerIndex(motionEvent)) - this.mInitialTouchY;
                if (!this.mEnterAnimationRunning) {
                    onMiniWindowTracking(y);
                }
            } else if (actionMasked != 3) {
                if (actionMasked == 6) {
                    handlePointerUp(motionEvent);
                }
            }
            return this.mTrackingMiniWindowRow;
        }
        onMiniWindowTrackingEnd();
        return this.mTrackingMiniWindowRow;
    }

    private final void onMiniWindowChildPicked(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        this.mPickedMiniWindowChild = miuiExpandableNotificationRow;
        this.mPickedChildWidth = miuiExpandableNotificationRow.getWidth();
        this.mPickedChildHeight = Math.max(miuiExpandableNotificationRow.getActualHeight() - miuiExpandableNotificationRow.getClipBottomAmount(), 0);
        int[] iArr = {0, 0};
        miuiExpandableNotificationRow.getLocationOnScreen(iArr);
        this.mPickedChildTop = iArr[1];
        int i = iArr[0];
        this.mPickedChildLeft = i;
        this.mPickedChildRight = i + this.mPickedChildWidth;
        this.mTouchCallback.onMiniWindowChildPicked(miuiExpandableNotificationRow);
    }

    private final void onMiniWindowTrackingStart() {
        this.mMaxTriggerThreshold = this.mContext.getResources().getDimension(C0012R$dimen.mini_window_max_trigger_threshold);
        this.mTriggerThreshold = this.mContext.getResources().getDimension(C0012R$dimen.mini_window_trigger_threshold);
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.setExpandAnimationRunning(true);
            this.mTrackingMiniWindowRow = true;
            MiuiExpandableNotificationRow miuiExpandableNotificationRow2 = this.mPickedMiniWindowChild;
            if (miuiExpandableNotificationRow2 != null) {
                this.mExpandedParams.setStartPosition(miuiExpandableNotificationRow2.getLocationOnScreen());
                this.mExpandedParams.setStartTranslationZ(miuiExpandableNotificationRow2.getTranslationZ());
                this.mExpandedParams.setStartClipTopAmount(miuiExpandableNotificationRow2.getClipTopAmount());
                this.mExpandedParams.setStartWidth(this.mPickedChildWidth);
                this.mExpandedParams.setStartHeight(this.mPickedChildHeight);
                this.mExpandedParams.setLeft(this.mPickedChildLeft);
                this.mExpandedParams.setTop(this.mPickedChildTop);
                this.mExpandedParams.setRight(this.mPickedChildRight);
                this.mExpandedParams.setBackgroundAlpha(1.0f);
                this.mExpandedParams.setIconAlpha(0.0f);
                this.mTouchCallback.onMiniWindowTrackingStart();
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void onMiniWindowTracking(float f) {
        updateTrackingOffset(f);
        this.mTouchCallback.onMiniWindowTrackingUpdate(f);
    }

    private final void onMiniWindowTrackingEnd() {
        if (!this.mEnterAnimationRunning && this.mTrackingMiniWindowRow) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            int height = this.mExpandedParams.getHeight() - this.mExpandedParams.getStartHeight();
            if (this.mVelocityTracker.getYVelocity(0) <= ((float) 1000) || ((float) height) <= this.mTriggerThreshold) {
                startResetToNotificationAnimation();
            } else {
                startEnterAndLaunchMiniWindow(MiniWindowEventReason.SPEED);
            }
        }
        this.mTouchingMiniWindowRow = false;
        this.mTrackingMiniWindowRow = false;
        this.mAbandonMiniWindowTracks = false;
        this.mVelocityTracker.clear();
        this.mTouchCallback.onMiniWindowTrackingEnd();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void onMiniWindowReset() {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.applyMiniWindowExpandParams(null);
        }
        MiuiExpandableNotificationRow miuiExpandableNotificationRow2 = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow2 != null) {
            miuiExpandableNotificationRow2.setExpandAnimationRunning(false);
        }
        this.mPickedMiniWindowChild = null;
        this.mEnterAnimationRunning = false;
        this.mTouchCallback.onMiniWindowReset();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void onExpandedParamsUpdated() {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.applyMiniWindowExpandParams(this.mExpandedParams);
        }
        this.mTouchCallback.onExpandedParamsUpdated(this.mExpandedParams);
    }

    private final void updateTrackingOffset(float f) {
        this.mExpandedParams.setAlpha(((float) 1) - RangesKt___RangesKt.coerceIn(f / this.mMaxTriggerThreshold, 0.0f, 1.0f));
        MiniWindowExpandParameters miniWindowExpandParameters = this.mExpandedParams;
        miniWindowExpandParameters.setBottom(miniWindowExpandParameters.getTop() + this.mPickedChildHeight + ((int) RangesKt___RangesKt.coerceAtLeast(f, 0.0f)));
        onExpandedParamsUpdated();
        if (f > this.mMaxTriggerThreshold) {
            startEnterAndLaunchMiniWindow(MiniWindowEventReason.DISTANCE);
        }
    }

    private final void startResetToNotificationAnimation() {
        ValueAnimator ofInt = ValueAnimator.ofInt(this.mExpandedParams.getBottom(), this.mExpandedParams.getTop() + this.mExpandedParams.getStartHeight());
        ofInt.setDuration(200L);
        ofInt.setInterpolator(new DecelerateInterpolator());
        ofInt.addUpdateListener(new AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1(this));
        ofInt.addListener(new AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2(this));
        ofInt.start();
    }

    private final void startEnterAndLaunchMiniWindow(MiniWindowEventReason miniWindowEventReason) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        String miniWindowTargetPkg = miuiExpandableNotificationRow != null ? miuiExpandableNotificationRow.getMiniWindowTargetPkg() : null;
        if (miniWindowTargetPkg != null) {
            startEnterMiniWindowAnimation();
            launchMiniWindowActivity(miniWindowEventReason);
            this.mHandler.sendEmptyMessageDelayed(1, 3000);
            ((AppMiniWindowManager) Dependency.get(AppMiniWindowManager.class)).registerOneshotForegroundWindowListener(miniWindowTargetPkg, new AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1(this));
        }
    }

    private final void startEnterMiniWindowAnimation() {
        this.mExpandedParams.setAlpha(0.0f);
        resetPickedChildAnimIfNeed();
        Rect rect = new Rect(this.mExpandedParams.getLeft(), this.mExpandedParams.getTop(), this.mExpandedParams.getRight(), this.mExpandedParams.getBottom());
        Rect freeformRect = MiuiMultiWindowUtils.getFreeformRect(this.mContext);
        freeformRect.right = freeformRect.left + ((int) (((float) freeformRect.width()) * MiuiMultiWindowUtils.sScale));
        freeformRect.bottom = freeformRect.top + ((int) (((float) freeformRect.height()) * MiuiMultiWindowUtils.sScale));
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(300L);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
        ofFloat.addUpdateListener(new AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1(this, rect, freeformRect));
        ofFloat.start();
        this.mEnterAnimationRunning = true;
        this.mTouchCallback.onStartMiniWindowExpandAnimation();
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
    }

    private final void resetPickedChildAnimIfNeed() {
        ExpandableViewState viewState;
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if ((miuiExpandableNotificationRow != null ? miuiExpandableNotificationRow.getScaleX() : 1.0f) == 1.0f) {
            MiuiExpandableNotificationRow miuiExpandableNotificationRow2 = this.mPickedMiniWindowChild;
            if ((miuiExpandableNotificationRow2 != null ? miuiExpandableNotificationRow2.getScaleY() : 1.0f) == 1.0f) {
                return;
            }
        }
        MiuiExpandableNotificationRow miuiExpandableNotificationRow3 = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow3 != null) {
            miuiExpandableNotificationRow3.setScaleX(1.0f);
            MiuiExpandableNotificationRow miuiExpandableNotificationRow4 = this.mPickedMiniWindowChild;
            if (miuiExpandableNotificationRow4 != null) {
                miuiExpandableNotificationRow4.setScaleY(1.0f);
                MiuiExpandableNotificationRow miuiExpandableNotificationRow5 = this.mPickedMiniWindowChild;
                if (miuiExpandableNotificationRow5 != null && (viewState = miuiExpandableNotificationRow5.getViewState()) != null && viewState.getTouchAnimating()) {
                    MiuiExpandableNotificationRow miuiExpandableNotificationRow6 = this.mPickedMiniWindowChild;
                    if (miuiExpandableNotificationRow6 != null) {
                        ExpandableViewState viewState2 = miuiExpandableNotificationRow6.getViewState();
                        if (viewState2 != null) {
                            viewState2.setTouchAnimating(false);
                        } else {
                            Intrinsics.throwNpe();
                            throw null;
                        }
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    private final void launchMiniWindowActivity(MiniWindowEventReason miniWindowEventReason) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            this.mEventTracker.track(MiniWindowEvents.INSTANCE.makeMiniWindowEvent(this.source, miniWindowEventReason));
            ((AppMiniWindowManager) Dependency.get(AppMiniWindowManager.class)).launchMiniWindowActivity(miuiExpandableNotificationRow.getMiniWindowTargetPkg(), miuiExpandableNotificationRow.getPendingIntent());
        }
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

    private final int getValidPointerIndex(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex >= 0) {
            return findPointerIndex;
        }
        this.mTrackingPointer = motionEvent.getPointerId(0);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void handleHideNotificationPanel() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setDuration(300L);
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1(this));
        ofFloat.addListener(new AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(this));
        ofFloat.start();
    }
}
