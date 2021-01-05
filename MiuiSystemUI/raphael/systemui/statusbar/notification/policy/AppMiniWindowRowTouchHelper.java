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
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventReason;
import com.miui.systemui.events.MiniWindowEventSource;
import com.miui.systemui.events.MiniWindowEvents;
import com.miui.systemui.util.HapticFeedBackImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper {
    private boolean mAbandonMiniWindowTracks;
    private final Context mContext;
    private boolean mEnterAnimationRunning;
    private final EventTracker mEventTracker;
    /* access modifiers changed from: private */
    public final MiniWindowExpandParameters mExpandedParams;
    /* access modifiers changed from: private */
    public final AppMiniWindowRowTouchHelper$mHandler$1 mHandler;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public float mMaxTriggerThreshold;
    /* access modifiers changed from: private */
    public final NotificationEntryManager mNotificationEntryManager;
    private int mPickedChildHeight;
    private int mPickedChildWidth;
    /* access modifiers changed from: private */
    public MiuiExpandableNotificationRow mPickedMiniWindowChild;
    /* access modifiers changed from: private */
    public final AppMiniWindowRowTouchCallback mTouchCallback;
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
                MiniWindowExpandParameters miniWindowExpandParameters = this.mExpandedParams;
                int[] startPosition = miniWindowExpandParameters.getStartPosition();
                int i = 0;
                miniWindowExpandParameters.setLeft(startPosition != null ? startPosition[0] : 0);
                MiniWindowExpandParameters miniWindowExpandParameters2 = this.mExpandedParams;
                int[] startPosition2 = miniWindowExpandParameters2.getStartPosition();
                if (startPosition2 != null) {
                    i = startPosition2[1];
                }
                miniWindowExpandParameters2.setTop(i);
                MiniWindowExpandParameters miniWindowExpandParameters3 = this.mExpandedParams;
                miniWindowExpandParameters3.setRight(miniWindowExpandParameters3.getLeft() + this.mPickedChildWidth);
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
    public final void onMiniWindowReset() {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.applyMiniWindowExpandParams((MiniWindowExpandParameters) null);
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
    public final void onExpandedParamsUpdated() {
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
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{this.mExpandedParams.getBottom(), this.mExpandedParams.getTop() + this.mExpandedParams.getStartHeight()});
        ofInt.setDuration(200);
        ofInt.setInterpolator(new DecelerateInterpolator());
        ofInt.addUpdateListener(new AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1(this));
        ofInt.addListener(new AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2(this));
        ofInt.start();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = r0.getPendingIntent();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void startEnterAndLaunchMiniWindow(com.miui.systemui.events.MiniWindowEventReason r5) {
        /*
            r4 = this;
            com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow r0 = r4.mPickedMiniWindowChild
            if (r0 == 0) goto L_0x000f
            android.app.PendingIntent r0 = r0.getPendingIntent()
            if (r0 == 0) goto L_0x000f
            java.lang.String r0 = r0.getCreatorPackage()
            goto L_0x0010
        L_0x000f:
            r0 = 0
        L_0x0010:
            if (r0 != 0) goto L_0x0013
            return
        L_0x0013:
            r4.startEnterMiniWindowAnimation()
            r4.launchMiniWindowActivity(r5)
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper$mHandler$1 r5 = r4.mHandler
            r1 = 1
            r2 = 3000(0xbb8, double:1.482E-320)
            r5.sendEmptyMessageDelayed(r1, r2)
            java.lang.Class<com.android.systemui.statusbar.notification.policy.AppMiniWindowManager> r5 = com.android.systemui.statusbar.notification.policy.AppMiniWindowManager.class
            java.lang.Object r5 = com.android.systemui.Dependency.get(r5)
            com.android.systemui.statusbar.notification.policy.AppMiniWindowManager r5 = (com.android.systemui.statusbar.notification.policy.AppMiniWindowManager) r5
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1 r1 = new com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1
            r1.<init>(r4)
            r5.registerOneshotForegroundWindowListener(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper.startEnterAndLaunchMiniWindow(com.miui.systemui.events.MiniWindowEventReason):void");
    }

    private final void startEnterMiniWindowAnimation() {
        this.mExpandedParams.setAlpha(0.0f);
        Rect rect = new Rect(this.mExpandedParams.getLeft(), this.mExpandedParams.getTop(), this.mExpandedParams.getRight(), this.mExpandedParams.getBottom());
        Rect freeformRect = MiuiMultiWindowUtils.getFreeformRect(this.mContext);
        freeformRect.right = freeformRect.left + ((int) (((float) freeformRect.width()) * MiuiMultiWindowUtils.sScale));
        freeformRect.bottom = freeformRect.top + ((int) (((float) freeformRect.height()) * MiuiMultiWindowUtils.sScale));
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(300);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
        ofFloat.addUpdateListener(new AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1(this, rect, freeformRect));
        ofFloat.start();
        this.mEnterAnimationRunning = true;
        this.mTouchCallback.onStartMiniWindowExpandAnimation();
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
    }

    private final void launchMiniWindowActivity(MiniWindowEventReason miniWindowEventReason) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            this.mEventTracker.track(MiniWindowEvents.INSTANCE.makeMiniWindowEvent(this.source, miniWindowEventReason));
            ((AppMiniWindowManager) Dependency.get(AppMiniWindowManager.class)).launchMiniWindowActivity(miuiExpandableNotificationRow.getPendingIntent());
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
    public final void handleHideNotificationPanel() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        ofFloat.setDuration(300);
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1(this));
        ofFloat.addListener(new AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(this));
        ofFloat.start();
    }
}
