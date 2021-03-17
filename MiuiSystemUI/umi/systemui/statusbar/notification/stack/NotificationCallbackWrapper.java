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

    public int getConstrainSwipeStartPosition() {
        return this.base.getConstrainSwipeStartPosition();
    }

    public boolean canChildBeDragged(@NotNull View view, int i) {
        Intrinsics.checkParameterIsNotNull(view, "animView");
        return this.base.canChildBeDragged(view, i);
    }

    public void onSnooze(@Nullable StatusBarNotification statusBarNotification, @Nullable NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.base.onSnooze(statusBarNotification, snoozeOption);
    }

    public void onSnooze(@Nullable StatusBarNotification statusBarNotification, int i) {
        this.base.onSnooze(statusBarNotification, i);
    }

    public boolean shouldDismissQuickly() {
        return this.base.shouldDismissQuickly();
    }

    public void onChildSnappedBack(@Nullable View view, float f) {
        this.base.onChildSnappedBack(view, f);
    }

    public void onBeginDrag(@Nullable View view) {
        this.base.onBeginDrag(view);
    }

    public void onDragCancelled(@Nullable View view) {
        this.base.onDragCancelled(view);
    }

    public View getChildAtPosition(@Nullable MotionEvent motionEvent) {
        return this.base.getChildAtPosition(motionEvent);
    }

    public void onChildDismissed(@Nullable View view) {
        this.base.onChildDismissed(view);
    }

    public boolean updateSwipeProgress(@Nullable View view, boolean z, float f) {
        return this.base.updateSwipeProgress(view, z, f);
    }

    public boolean canChildBeDismissed(@Nullable View view) {
        return this.base.canChildBeDismissed(view);
    }

    public boolean isAntiFalsingNeeded() {
        return this.base.isAntiFalsingNeeded();
    }

    public void handleChildViewDismissed(@Nullable View view) {
        this.base.handleChildViewDismissed(view);
    }

    public void onDismiss() {
        this.base.onDismiss();
    }

    public float getFalsingThresholdFactor() {
        return this.base.getFalsingThresholdFactor();
    }
}
