package com.android.systemui.statusbar.phone;

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
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventReason;
import com.miui.systemui.events.MiniWindowEventSource;
import com.miui.systemui.events.MiniWindowEvents;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
public final class MiuiHeadsUpTouchHelper extends HeadsUpTouchHelper {
    private boolean mAbandonMiniWindowTracks;
    private final AppMiniWindowManager mAppMiniWindowManager = ((AppMiniWindowManager) Dependency.get(AppMiniWindowManager.class));
    private final HeadsUpTouchHelper.Callback mCallback;
    private final NotificationListContainer mContainer;
    private final Context mContext;
    private boolean mEnterAnimationRunning;
    private final EventTracker mEventTracker;
    /* access modifiers changed from: private */
    public final MiniWindowExpandParameters mExpandedParams;
    /* access modifiers changed from: private */
    public final MiuiHeadsUpTouchHelper$mHandler$1 mHandler;
    /* access modifiers changed from: private */
    public final HeadsUpManagerPhone mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public float mMaxTriggerThreshold;
    /* access modifiers changed from: private */
    public final NotificationEntryManager mNotificationEntryManager;
    private final NotificationPanelViewController mPanel;
    private int mPickedChildHeight;
    private int mPickedChildWidth;
    /* access modifiers changed from: private */
    public MiuiExpandableNotificationRow mPickedMiniWindowChild;
    /* access modifiers changed from: private */
    public final NotificationStackScrollLayout mStackScrollLayout;
    private final int mTouchSlop;
    private boolean mTouchingMiniWindowHeadsUp;
    private boolean mTrackingMiniWindowHeadsUp;
    private int mTrackingPointer;
    private float mTriggerThreshold;
    private final VelocityTracker mVelocityTracker;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiHeadsUpTouchHelper(@NotNull HeadsUpManagerPhone headsUpManagerPhone, @NotNull HeadsUpTouchHelper.Callback callback, @NotNull NotificationPanelViewController notificationPanelViewController, @NotNull NotificationListContainer notificationListContainer, @NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationStackScrollLayout notificationStackScrollLayout, @NotNull EventTracker eventTracker) {
        super(headsUpManagerPhone, callback, notificationPanelViewController);
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        Intrinsics.checkParameterIsNotNull(notificationPanelViewController, "notificationPanelView");
        Intrinsics.checkParameterIsNotNull(notificationListContainer, "container");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "notificationStackScrollLayout");
        Intrinsics.checkParameterIsNotNull(eventTracker, "eventTracker");
        this.mEventTracker = eventTracker;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mPanel = notificationPanelViewController;
        this.mContainer = notificationListContainer;
        this.mCallback = callback;
        this.mNotificationEntryManager = notificationEntryManager;
        this.mStackScrollLayout = notificationStackScrollLayout;
        this.mContext = callback.getContext();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(callback.getContext());
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(callback.context)");
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mExpandedParams = new MiniWindowExpandParameters();
        this.mHandler = new MiuiHeadsUpTouchHelper$mHandler$1(this, Looper.getMainLooper());
    }

    public boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if ((!this.mTouchingMiniWindowHeadsUp || this.mAbandonMiniWindowTracks) && motionEvent.getActionMasked() == 2) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        int validPointerIndex = getValidPointerIndex(motionEvent);
        float x = motionEvent.getX(validPointerIndex);
        float y = motionEvent.getY(validPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    if (this.mTouchingMiniWindowHeadsUp && f < ((float) (-this.mTouchSlop))) {
                        this.mAbandonMiniWindowTracks = true;
                    }
                    if (this.mTouchingMiniWindowHeadsUp && !this.mTrackingMiniWindowHeadsUp && f > ((float) this.mTouchSlop) && Math.abs(f) > Math.abs(x - this.mInitialTouchX)) {
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
            ExpandableView childAtRawPosition = this.mCallback.getChildAtRawPosition(x, y);
            if (childAtRawPosition instanceof MiuiExpandableNotificationRow) {
                MiuiExpandableNotificationRow miuiExpandableNotificationRow = (MiuiExpandableNotificationRow) childAtRawPosition;
                if (miuiExpandableNotificationRow.canSlideToMiniWindow()) {
                    boolean z = !this.mCallback.isExpanded() && miuiExpandableNotificationRow.isHeadsUp() && childAtRawPosition.isPinned();
                    this.mTouchingMiniWindowHeadsUp = z;
                    if (z) {
                        onMiniWindowChildPicked(miuiExpandableNotificationRow);
                    }
                }
            }
        }
        if (this.mTrackingMiniWindowHeadsUp || super.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        return false;
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

    public boolean onTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        this.mVelocityTracker.addMovement(motionEvent);
        if ((!this.mTrackingMiniWindowHeadsUp || this.mAbandonMiniWindowTracks) && motionEvent.getActionMasked() == 2) {
            return super.onTouchEvent(motionEvent);
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
            return this.mTrackingMiniWindowHeadsUp;
        }
        onMiniWindowTrackingEnd();
        return this.mTrackingMiniWindowHeadsUp;
    }

    private final int getValidPointerIndex(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex >= 0) {
            return findPointerIndex;
        }
        this.mTrackingPointer = motionEvent.getPointerId(0);
        return 0;
    }

    private final void onMiniWindowChildPicked(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        this.mPickedMiniWindowChild = miuiExpandableNotificationRow;
        this.mPickedChildWidth = miuiExpandableNotificationRow.getWidth();
        this.mPickedChildHeight = Math.max(miuiExpandableNotificationRow.getActualHeight() - miuiExpandableNotificationRow.getClipBottomAmount(), 0);
    }

    private final void onMiniWindowTrackingStart() {
        Context context = this.mContext;
        Intrinsics.checkExpressionValueIsNotNull(context, "mContext");
        this.mMaxTriggerThreshold = context.getResources().getDimension(C0012R$dimen.mini_window_max_trigger_threshold);
        Context context2 = this.mContext;
        Intrinsics.checkExpressionValueIsNotNull(context2, "mContext");
        this.mTriggerThreshold = context2.getResources().getDimension(C0012R$dimen.mini_window_trigger_threshold);
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.setExpandAnimationRunning(true);
            setTrackingHeadsUp(true);
            this.mTrackingMiniWindowHeadsUp = true;
            this.mHeadsUpManager.extendHeadsUp();
            this.mPanel.clearNotificationEffects();
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
    }

    private final void onMiniWindowTrackingEnd() {
        if (!this.mEnterAnimationRunning && this.mTrackingMiniWindowHeadsUp) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            int height = this.mExpandedParams.getHeight() - this.mExpandedParams.getStartHeight();
            if (this.mVelocityTracker.getYVelocity(0) <= ((float) 1000) || ((float) height) <= this.mTriggerThreshold) {
                startResetToNotificationAnimation();
            } else {
                startEnterAndLaunchMiniWindow(MiniWindowEventReason.SPEED);
            }
        }
        this.mTouchingMiniWindowHeadsUp = false;
        this.mTrackingMiniWindowHeadsUp = false;
        this.mAbandonMiniWindowTracks = false;
        this.mVelocityTracker.clear();
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
        this.mContainer.applyExpandAnimationParams((ActivityLaunchAnimator.ExpandAnimationParameters) null);
        this.mPickedMiniWindowChild = null;
        setTrackingHeadsUp(false);
        this.mEnterAnimationRunning = false;
    }

    /* access modifiers changed from: private */
    public final void onExpandedParamsUpdated() {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.applyMiniWindowExpandParams(this.mExpandedParams);
        }
        this.mContainer.applyExpandAnimationParams(this.mExpandedParams);
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
        ofInt.addUpdateListener(new MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1(this));
        ofInt.addListener(new MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2(this));
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
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper$mHandler$1 r5 = r4.mHandler
            r1 = 1
            r2 = 3000(0xbb8, double:1.482E-320)
            r5.sendEmptyMessageDelayed(r1, r2)
            com.android.systemui.statusbar.notification.policy.AppMiniWindowManager r5 = r4.mAppMiniWindowManager
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper$startEnterAndLaunchMiniWindow$1 r1 = new com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper$startEnterAndLaunchMiniWindow$1
            r1.<init>(r4)
            r5.registerOneshotForegroundWindowListener(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper.startEnterAndLaunchMiniWindow(com.miui.systemui.events.MiniWindowEventReason):void");
    }

    private final void startEnterMiniWindowAnimation() {
        this.mExpandedParams.setAlpha(0.0f);
        Rect rect = new Rect(this.mExpandedParams.getLeft(), this.mExpandedParams.getTop(), this.mExpandedParams.getRight(), this.mExpandedParams.getBottom());
        Rect freeformRect = MiuiMultiWindowUtils.getFreeformRect(this.mCallback.getContext());
        freeformRect.right = freeformRect.left + ((int) (((float) freeformRect.width()) * MiuiMultiWindowUtils.sScale));
        freeformRect.bottom = freeformRect.top + ((int) (((float) freeformRect.height()) * MiuiMultiWindowUtils.sScale));
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(300);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
        ofFloat.addUpdateListener(new MiuiHeadsUpTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1(this, rect, freeformRect));
        ofFloat.start();
        this.mEnterAnimationRunning = true;
    }

    private final void launchMiniWindowActivity(MiniWindowEventReason miniWindowEventReason) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            this.mEventTracker.track(MiniWindowEvents.INSTANCE.makeMiniWindowEvent(MiniWindowEventSource.HEADS_UP, miniWindowEventReason));
            this.mAppMiniWindowManager.launchMiniWindowActivity(miuiExpandableNotificationRow.getPendingIntent());
        }
    }

    /* access modifiers changed from: private */
    public final void handleHideNotificationPanel() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        ofFloat.setDuration(300);
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1(this));
        ofFloat.addListener(new MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(this));
        ofFloat.start();
    }

    public final boolean isTrackingMiniWindowHeadsUp$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.mTrackingMiniWindowHeadsUp;
    }
}
