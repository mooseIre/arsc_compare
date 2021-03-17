package com.android.systemui.statusbar.notification.stack;

import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationSwipeCallback.kt */
public class NotificationCallbackWrapper implements NotificationSwipeHelper.NotificationCallback {
    private final NotificationSwipeHelper.NotificationCallback base;

    public NotificationCallbackWrapper(@NotNull NotificationSwipeHelper.NotificationCallback notificationCallback) {
        Intrinsics.checkParameterIsNotNull(notificationCallback, "base");
        this.base = notificationCallback;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public int getConstrainSwipeStartPosition() {
        return this.base.getConstrainSwipeStartPosition();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean canChildBeDragged(@NotNull View view, int i) {
        Intrinsics.checkParameterIsNotNull(view, "animView");
        return this.base.canChildBeDragged(view, i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
    public void onSnooze(@Nullable StatusBarNotification statusBarNotification, @Nullable NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.base.onSnooze(statusBarNotification, snoozeOption);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
    public void onSnooze(@Nullable StatusBarNotification statusBarNotification, int i) {
        this.base.onSnooze(statusBarNotification, i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
    public boolean shouldDismissQuickly() {
        return this.base.shouldDismissQuickly();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildSnappedBack(@Nullable View view, float f) {
        this.base.onChildSnappedBack(view, f);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onBeginDrag(@Nullable View view) {
        this.base.onBeginDrag(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onDragCancelled(@Nullable View view) {
        this.base.onDragCancelled(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public View getChildAtPosition(@Nullable MotionEvent motionEvent) {
        return this.base.getChildAtPosition(motionEvent);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public void onChildDismissed(@Nullable View view) {
        this.base.onChildDismissed(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean updateSwipeProgress(@Nullable View view, boolean z, float f) {
        return this.base.updateSwipeProgress(view, z, f);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean canChildBeDismissed(@Nullable View view) {
        return this.base.canChildBeDismissed(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean isAntiFalsingNeeded() {
        return this.base.isAntiFalsingNeeded();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
    public void handleChildViewDismissed(@Nullable View view) {
        this.base.handleChildViewDismissed(view);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
    public void onDismiss() {
        this.base.onDismiss();
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public float getFalsingThresholdFactor() {
        return this.base.getFalsingThresholdFactor();
    }
}
